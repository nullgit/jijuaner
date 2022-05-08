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


index_bp = Blueprint('index', __name__, url_prefix='/index')


class IndexValueNameFunddb:
    def __init__(self, index_code: str, index_name: str, start_time: str) -> None:
        self.index_name, self.index_code, self.start_time = index_name, index_code, start_time


class IndexValueNameFunddbService:
    KEY = 'jijuaner:index_value_name_funddb'
    OLD_KEY = 'jijuaner:index_value_name_funddb:old'

    @staticmethod
    def request() -> DataFrame:
        pd = ak.index_value_name_funddb()
        return pd

    def request_and_save(self) -> Optional[List[IndexValueNameFunddb]]:
        try:
            all_index = IndexValueNameFunddb.request()
            all_index.columns = ['index_name', 'index_code', 'start_time']
            all_index['start_time'] = all_index['start_time'].map(
                lambda x: '-' if x is pd.NaT else x.strftime('%Y-%m-%d'))
            all_index['start_time'] = datetime_series_to_date_strs(
                all_index['start_time'])
            data = [IndexValueNameFunddb(x[0], x[1], x[2])
                    for x in all_index.values.tolist()]
            expiration_time = int(timedelta(weeks=4).total_seconds())  # 数据4周后才需删除
            s = jsonpickle.encode(data)
            redis.setex(IndexValueNameFunddbService.KEY, expiration_time, s)
            # 复制一份
            redis.set(IndexValueNameFunddbService.OLD_KEY, s)
        except Exception:
            logging.error('IndexValueNameFunddbService request 出错')
            # 如果出错直接返回None
            return None
        return data

    def list(self) -> Optional[List[IndexValueNameFunddb]]:
        data = decode_if_not_none(redis.get(IndexValueNameFunddbService.KEY))
        if data is None:
            data = self.request_and_save()
            # 接口出现错误, 使用原来的数据
            if data is None:
                data = decode_if_not_none(redis.get(IndexValueNameFunddbService.OLD_KEY))
        return data


class IndexValueHistFunddb(ModelWithExpirationTime):
    def __init__(self, expiration_time: datetime = None,
                 index_name: str = '',
                 pe_x: List[str] = [], pe_y: List[str] = [],
                 pb_x: List[str] = [], pb_y: List[str] = [],
                 pd_x: List[str] = [], pd_y: List[str] = []) -> None:
        ModelWithExpirationTime.__init__(self, expiration_time)
        self.index_name = index_name
        self.pe_x, self.pe_y = pe_x, pe_y
        self.pb_x, self.pb_y = pb_x, pb_y
        self.pd_x, self.pd_y = pd_x, pd_y


class IndexValueHistFunddbService:
    def __init__(self) -> None:
        self.mongo = mongo.db.index_value_hist_funddb

    @staticmethod
    def request(index_name: str, indicator: str) -> IndexValueHistFunddb:
        pd = ak.index_value_hist_funddb(symbol=index_name, indicator=indicator)
        return pd

    def request_and_save(self, index_name: str) -> Optional[IndexValueHistFunddb]:
        try:
            future1 = thread_pool.submit(
                IndexValueHistFunddbService.request, index_name, '市盈率')
            future2 = thread_pool.submit(
                IndexValueHistFunddbService.request, index_name, '市净率')
            future3 = thread_pool.submit(
                IndexValueHistFunddbService.request, index_name, '股息率')
            df1 = future1.result()
            df2 = future2.result()
            df3 = future3.result()
            expiration_time = datetime.now() + timedelta(hours=6)
            data = IndexValueHistFunddb(
                expiration_time,
                index_name,
                datetime_series_to_date_strs(df1['日期']).to_list(),
                num_series_to_strs(df1['市盈率']).to_list(),
                datetime_series_to_date_strs(df2['日期']).to_list(),
                num_series_to_strs(df2['市净率']).to_list(),
                datetime_series_to_date_strs(df3['日期']).to_list(),
                num_series_to_strs(df3['股息率']).to_list(),
            )
            self.mongo.insert_one(data.__dict__)
        except Exception as e:
            print(e)
            return None
        return data

    def get(self, index_name: str) -> Optional[IndexValueHistFunddb]:
        d = self.mongo.find_one({'index_name': index_name})
        # 如果数据库中还没有这条记录或该记录过期, 重新request_and_save
        if d is None or datetime.now() > d['expiration_time']:
            data = self.request_and_save(index_name)
            # index_name参数错误
            # TODO 应校验index_name是否存在
            # 或接口出现错误
            if data is None and d is not None:
                # 使用原来的数据
                data = IndexValueHistFunddb()
                data.__dict__.update(d)
        else:
            data = IndexValueHistFunddb()
            data.__dict__.update(d)
        return data


index_value_name_funddb_service = IndexValueNameFunddbService()
index_value_hist_funddb_service = IndexValueHistFunddbService()


@index_bp.route('/names')
def list_index() -> str:
    data = index_value_name_funddb_service.list()
    return jsonpickle.encode(data)


@index_bp.route('/hist/<index_name>')
def get_hist(index_name: str) -> str:
    data = index_value_hist_funddb_service.get(index_name)
    return jsonpickle.encode(data)
