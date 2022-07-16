from datetime import datetime, timedelta
from typing import Optional

import jsonpickle
import jsonpickle.ext.pandas as jsonpickle_pd
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from bp.bond import bond_rate_service
from bp.index import index_value_hist_funddb_service
from databus import redis
from pandas import DataFrame, Series
from pyecharts import options as opts
from pyecharts.charts import Line, Page
from pyecharts.globals import ThemeType
from pyecharts.options import TooltipOpts
from utils.back_test import BackTest
from utils.const import AC_WORTH, DATE
from utils.utils import decode_if_not_none

jsonpickle_pd.register_handlers()

KEY = 'jijuaner:risk_premium'


def save_risk_premium_data(index_name: str, bond_rate_key: str) -> DataFrame:
    index_hist = index_value_hist_funddb_service.get(index_name)
    bond_rate = bond_rate_service.get(bond_rate_key)

    index_ser = Series(
        np.array(index_hist.pe_y, dtype=np.float32), index=pd.to_datetime(index_hist.pe_x))
    bond_rate_ser = Series(
        np.array(bond_rate.y, dtype=np.float32), index=pd.to_datetime(bond_rate.x))

    res = -(1 / index_ser * 100 - bond_rate_ser)  # 市盈率倒数-无风险利率
    res = res.dropna()
    redis.setex(f'{KEY}:{index_name}_{bond_rate_key}',
                int(timedelta(hours=6).total_seconds()), jsonpickle.encode(res))
    return res


def get_risk_premium_data(index_name: str, bond_rate_key: str) -> Optional[DataFrame]:
    key = f'{KEY}:{index_name}_{bond_rate_key}'
    data = decode_if_not_none(redis.get(key))
    if data is None:
        try:
            data = save_risk_premium_data(index_name, bond_rate_key)
        except Exception as e:
            print(e)
            return None
    return data


def get_fig(index_name: str, bond_rate_key: str):
    res = get_risk_premium_data(index_name, bond_rate_key)
    desc = res.describe()
    plt.plot(res)
    plt.plot(res.index, [desc['25%']] * len(res))
    plt.plot(res.index, [desc['50%']] * len(res))
    plt.plot(res.index, [desc['75%']] * len(res))
    plt.draw()
    plt.savefig(
        f'./output/img_{index_name}_{bond_rate_key}.png')


def back_test_risk_premium_strategy(fund_code: str):
    tester = RiskPremiumBackTest(fund_code)
    tester.indexes['risk'] = get_risk_premium_data(
        '沪深300', '中国国债收益率10年').astype('float64')
    tester.cash = 1000000
    tester.start_time = datetime.strptime('2010-01-01', '%Y-%m-%d')
    tester.stop_time = datetime.strptime('2022-07-08', '%Y-%m-%d')
    tester.execute()
    print(tester.records['value'][-1])


class RiskPremiumBackTest(BackTest):
    def __init__(self, fund_code: str, debug: bool = False) -> None:
        super().__init__(fund_code, debug)

    def before_execute(self) -> None:
        self.records['date'] = []
        self.records['25%'] = []
        self.records['75%'] = []
        self.records['now'] = []

    def after_execute(self) -> None:
        page = Page('回测结果')
        opt = opts.InitOpts(theme=ThemeType.LIGHT, bg_color='white')
        tooltip_opt = TooltipOpts(trigger='axis')
        index_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.records['date']) \
            .add_yaxis("25%", self.records['25%']) \
            .add_yaxis('75%', self.records['75%']) \
            .add_yaxis('now', self.records['now'])
        fund_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.info[DATE].truncate(before=self.start_time).truncate(after=self.stop_time).to_list()) \
            .add_yaxis('ac_worth', self.info[AC_WORTH].truncate(before=self.start_time).truncate(after=self.stop_time).to_list())
        record_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.records['date']) \
            .add_yaxis('cash', self.records['cash']) \
            .add_yaxis('value', self.records['value'])
        page.add(fund_line).add(index_line).add(record_line)
        page.render('./output/风险溢价策略回测结果.html')


    def strategy(self) -> None:
        # 当风险溢价位于25%区间时, 加仓; 高于25%区间时, 减仓
        if self.indexes['now'] > self.indexes['75%']:
            self.sell(10000)
        elif self.indexes['now'] < self.indexes['25%']:
            self.buy(10000)

    def before_step(self) -> None:
        risk: Series = self.indexes['risk']
        prevs = risk.truncate(after=self.now)
        self.indexes['25%'] = prevs.quantile(0.25)
        self.indexes['75%'] = prevs.quantile(0.75)
        self.indexes['now'] = prevs.iloc[-1]

    def after_step(self) -> None:
        self.records['date'].append(self.now)
        self.records['25%'].append(self.indexes['25%'])
        self.records['75%'].append(self.indexes['75%'])
        self.records['now'].append(self.indexes['now'])
        self.records['cash'].append(self.cash)
        self.records['value'].append(self.compute_value())
