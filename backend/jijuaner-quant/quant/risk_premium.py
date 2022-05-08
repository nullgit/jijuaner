import akshare as ak
import pandas as pd
from pandas import DataFrame, Series
import matplotlib.pyplot as plt
import numpy as np
from datetime import datetime
from bp.bond import bond_rate_service
from bp.index import index_value_hist_funddb_service


def get_fig(index_name: str, bond_rate_key: str):
    index_hist = index_value_hist_funddb_service.get(index_name)
    bond_rate = bond_rate_service.get(bond_rate_key)

    index_ser = Series(np.array(index_hist.pb_y, dtype=np.float32), index=pd.to_datetime(index_hist.pb_x))
    bond_rate_ser = Series(np.array(bond_rate.y, dtype=np.float32), index=pd.to_datetime(bond_rate.x))

    res = -(1 / index_ser * 100 - bond_rate_ser)
    res = res.dropna()
    desc = res.describe()
    plt.plot(res)
    plt.plot(res.index, [desc['25%']] * len(res))
    plt.plot(res.index, [desc['50%']] * len(res))
    plt.plot(res.index, [desc['75%']] * len(res))
    plt.draw()
    plt.savefig(f'./backend/jijuaner-quant/quant/img/img_{index_name}_{bond_rate_key}.png')
