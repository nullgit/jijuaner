from typing import Any, Optional
from datetime import datetime
import jsonpickle
from pandas import Series
import pandas as pd


class ModelWithExpirationTime:
    def __init__(self, expiration_time: datetime) -> None:
        self.expiration_time = expiration_time
        self.data: Any


def get_data_if_within_period(s: str) -> Optional[Any]:
    if s is None:
        return None
    data_with_expiration_time: ModelWithExpirationTime = jsonpickle.decode(s)
    if datetime.now() > data_with_expiration_time.expiration_time:
        return None
    return data_with_expiration_time.data

