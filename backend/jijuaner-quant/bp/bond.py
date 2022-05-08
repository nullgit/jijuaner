from datetime import datetime, timedelta
import pandas as pd
from pandas import DataFrame
import akshare as ak
import asyncio
from flask import Blueprint
from databus import redis, mongo
from utils.model import ModelWithExpirationTime
from typing import List, Optional
from utils.model import get_data_if_within_period
from utils.utils import datetime_series_to_date_strs, num_series_to_strs, decode_if_not_none, to_date_str, date_str_to_datetime
from databus import thread_pool
import jsonpickle
import logging

bond_bp = Blueprint('bond', __name__, url_prefix='/bond')

class BondRate:
    def __init__(self, x: List[str] = [], y: List[str] = []) -> None:
        self.x, self.y = x, y


class BondRateService():
    KEY = 'jijuaner:quant:bond_rate'
    OLD_KEY = 'jijuaner:quant:bond_rate:old'
    KEYS = [
        '中国国债收益率2年', '中国国债收益率5年', '中国国债收益率10年', '中国国债收益率30年',
        '美国国债收益率2年', '美国国债收益率5年', '美国国债收益率10年', '美国国债收益率30年',
    ]

    @staticmethod
    def request() -> DataFrame:
        pd = ak.bond_zh_us_rate()
        return pd

    def request_and_save(self, key: str) -> Optional[BondRate]:
        try:
            bond_rates = BondRateService.request()
            # 中国国债收益率2年
            for k in BondRateService.KEYS:
                df = DataFrame(
                    {'date': bond_rates['日期'], k: bond_rates[k]})
                df = df.dropna()
                data = BondRate(datetime_series_to_date_strs(df['date']).to_list(),
                                num_series_to_strs(df[k]).to_list())
                s = jsonpickle.encode(data)
                redis.hset(BondRateService.KEY, k, s)
                # 复制一份
                redis.hset(BondRateService.OLD_KEY, k, s)
                if key == k:
                    ret = data
            expiration_time = int(timedelta(hours=6).total_seconds())
            redis.expire(BondRateService.KEY, expiration_time)
        except Exception as e:
            print(e)
            logging.error('BondRateService request 出错')
            return None
        return ret

    def get(self, key: str) -> Optional[BondRate]:
        data = decode_if_not_none(redis.hget(BondRateService.KEY, key))
        if data is None:
            data = self.request_and_save(key)
            # 接口出现错误, 使用原来的数据
            if data is None:
                data = decode_if_not_none(redis.get(BondRateService.OLD_KEY))
        return data


bond_rate_service = BondRateService()


@bond_bp.route('/<key>')
def list_index(key: str) -> str:
    data = bond_rate_service.get(key)
    return jsonpickle.encode(data)
