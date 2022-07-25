from datetime import datetime

import numpy as np
import pandas as pd
from pyecharts import options as opts
from pyecharts.charts import Line, Page
from pyecharts.globals import ThemeType
from pyecharts.options import TooltipOpts
from utils.back_test import BackTest
from utils.const import AC_WORTH, DATE, CASH, VALUE, NOW


def back_test_average_strategy(fund_code: str):
    tester = AverageBackTest(fund_code)
    tester.cash = 1000000
    tester.start_time = datetime.strptime('2010-01-01', '%Y-%m-%d')
    tester.stop_time = datetime.strptime('2022-07-08', '%Y-%m-%d')
    tester.execute()
    print(tester.records[VALUE][-1])


class AverageBackTest(BackTest):
    def __init__(self, fund_code: str, debug: bool = False) -> None:
        super().__init__(fund_code, debug)

    def before_execute(self) -> None:
        self.records[DATE] = []
        self.records['1.25'] = []
        self.records['0.75'] = []
        self.records[NOW] = []
        self.records['avg'] = []

    def after_execute(self) -> None:
        page = Page('回测结果')
        opt = opts.InitOpts(theme=ThemeType.LIGHT, bg_color='white')
        tooltip_opt = TooltipOpts(trigger='axis')
        index_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.records[DATE]) \
            .add_yaxis('1.25', self.records['1.25']) \
            .add_yaxis('0.75', self.records['0.75']) \
            .add_yaxis(NOW, self.records[NOW]) \
            .add_yaxis('avg', self.records['avg'])
        fund_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.info[DATE].truncate(before=self.start_time).truncate(after=self.stop_time).to_list()) \
            .add_yaxis('ac_worth', self.info[AC_WORTH].truncate(before=self.start_time).truncate(after=self.stop_time).to_list())
        record_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.records[DATE]) \
            .add_yaxis(CASH, self.records[CASH]) \
            .add_yaxis(VALUE, self.records[VALUE])
        page.add(fund_line).add(index_line).add(record_line)
        page.render('./output/均线策略回测结果.html')

    def strategy(self) -> None:
        # 当超过均线的125%时, 加仓; 当低于均线的75%时, 减仓
        if np.isnan(self.indexes['1.25']):
            return
        if self.indexes[NOW] > self.indexes['1.25']:
            self.sell(1000)
        elif self.indexes[NOW] < self.indexes['0.75']:
            self.buy(10000)

    def before_step(self) -> None:
        prevs = self.info[AC_WORTH].truncate(after=self.now)
        self.indexes['avg'] = prevs.tail(180).mean()
        self.indexes['1.25'] = self.indexes['avg'] * 1.10
        self.indexes['0.75'] = self.indexes['avg'] * 0.95
        self.indexes[NOW] = prevs.iloc[-1]

    def after_step(self) -> None:
        self.records[DATE].append(self.now)
        self.records['1.25'].append(self.indexes['1.25'])
        self.records['0.75'].append(self.indexes['0.75'])
        self.records['avg'].append(self.indexes['avg'])
        self.records[NOW].append(self.indexes[NOW])
        self.records[CASH].append(self.cash)
        self.records[VALUE].append(self.compute_value())
