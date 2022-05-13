import pandas as pd
from pandas import Series, DataFrame
import jsonpickle
from typing import Any, Optional
from datetime import datetime


# datetime utils


DATE_FORMATTER = '%Y-%m-%d'


def to_date_str(d: datetime) -> str:
    return d.strftime(DATE_FORMATTER)


def date_str_to_datetime(s: str) -> datetime:
    return datetime.strptime(s, DATE_FORMATTER)


# pandas utils


def datetime_series_to_date_strs(datetimes: Series) -> Series:
    return datetimes.map(lambda x: '-' if x is pd.NaT else x.strftime(DATE_FORMATTER))


def num_series_to_strs(series: Series) -> Series:
    # 当x为np.nan时, 返回'nan'
    return series.map(lambda x:  str(x))


def get_cur_or_last_date_row(cur: datetime, df: DataFrame) -> Optional[pd.core.series.Series]:
    lasts = df.truncate(after=cur)
    if len(lasts) == 0:
        return None
    return lasts.iloc[-1]


def get_cur_or_next_date_row(cur: datetime, df: DataFrame) -> Optional[pd.core.series.Series]:
    nexts = df.truncate(before=cur)
    if len(nexts) == 0:
        return None
    return nexts.iloc[0]


# jsonpickle utils


def decode_if_not_none(s: str) -> Any:
    return jsonpickle.decode(s) if s is not None else None
