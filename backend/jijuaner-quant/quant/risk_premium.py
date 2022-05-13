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
from utils.back_test import BackTest
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
    tester = BackTest(fund_code)
    tester.indexes['risk'] = get_risk_premium_data(
        '沪深300', '中国国债收益率10年').astype('float64')
    tester.cash = 1000000
    tester.start_time = datetime.strptime('2010-01-01', '%Y-%m-%d')
    tester.stop_time = datetime.strptime('2022-05-30', '%Y-%m-%d')
    tester.execute()
    print(tester.records['value'][-1])
