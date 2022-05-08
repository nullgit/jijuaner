import pandas as pd
from pandas import Series
import jsonpickle
from typing import Any
from datetime import datetime

DATE_FORMATTER = '%Y-%m-%d'

def to_date_str(d: datetime) -> str:
    return d.strftime(DATE_FORMATTER)

def date_str_to_datetime(s: str) -> datetime:
    return datetime.strptime(s, DATE_FORMATTER)

def datetime_series_to_date_strs(datetimes: Series) -> Series:
    return datetimes.map(lambda x: '-' if x is pd.NaT else x.strftime(DATE_FORMATTER))

def num_series_to_strs(series: Series) -> Series:
    # 当x为np.nan时, 返回'nan'
    return series.map(lambda x:  str(x))

def decode_if_not_none(s: str) -> Any:
    return jsonpickle.decode(s) if s is not None else None

