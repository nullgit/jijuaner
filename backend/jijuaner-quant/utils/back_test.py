from datetime import datetime, timedelta
from enum import Enum, auto
from typing import Dict, List

import numpy as np
import pandas as pd
import requests
from pandas import DataFrame
from sortedcollections import SortedList
from utils.const import AC_WORTH, DATE, NET_WORTH
from utils.utils import get_cur_or_next_date_row


class OrderShare:
    '''已下订单的份额类'''

    def __init__(self, date: datetime, share: float, amount: float, ac_worth: float) -> None:
        self.date = date
        self.share = share
        self.amount = amount
        self.ac_worth = ac_worth
        # self.net_worth : float


class MessageType(Enum):
    BUY = auto()
    SELL = auto()


class Message:
    def __init__(self, date: datetime, type: MessageType, amount: float = 0.0, share: float = 0.0) -> None:
        self.date = date
        self.type = type
        self.amount = amount  # 买入金额
        self.share = share  # 卖出份额


class BuyRate:
    def __init__(self, amount: float, rate: float) -> None:
        self.amount = amount
        self.rate = rate


class SellRate:
    def __init__(self, holding_time: timedelta, rate: float) -> None:
        self.holding_time = holding_time
        self.rate = rate


class BackTest:
    def __init__(self, fund_code: str, debug: bool = False) -> None:
        # TODO 通过nacos实例获取数据
        with requests.get(f'http://localhost:10000/fund/fundInfo/{fund_code}') as resp:
            data = resp.json()['data']
            df = DataFrame({
                DATE: data['x'],
                NET_WORTH: np.array(data['netWorthTrend'], dtype=np.float64),
                AC_WORTH: np.array(data['acWorthTrend'], dtype=np.float64),
            })
            df[DATE] = df[DATE].apply(
                lambda x: datetime.fromtimestamp(float(x)/1000))
            df.index = df[DATE]
        self.info: DataFrame = df
        self.total_share: float = 0.0
        self.cash: float = 0.0
        self.sell_rates: List[SellRate] = [
            SellRate(timedelta(7), 1.50 * 0.01),
            SellRate(timedelta(30), 0.50 * 0.01),
            SellRate(timedelta(365), 0.10 * 0.01),
        ]  # TODO 通过接口获取手续费信息
        self.buy_rates: List[BuyRate] = [
            BuyRate(0, 0.15 * 0.01),
            BuyRate(1000000, 0.12 * 0.01),
            # TODO 大于500万后固定费用
        ]
        self.min_buy_amount: float = 10.0
        self.T = 1
        self.reset(debug=debug, cash=0.0)
        self.now: datetime
        self.now_row: pd.core.series.Series
        self.start_time: datetime
        self.stop_time: datetime

    def _reset_shares(self) -> None:
        self.shares: SortedList[OrderShare] = SortedList(
            key=lambda x: x.date)  # 已经成功的买入份额, 按买入的时间顺序排队

    def _reset_queue(self) -> None:
        self.queue: SortedList[Message] = SortedList(
            key=lambda x: x.date)  # 消息队列

    def _reset_indexes(self) -> None:
        self.indexes: Dict = {}

    def _reset_record(self) -> None:
        self.records: Dict = {'cash': [], 'value': []}

    def reset(self, start_time: datetime = None, stop_time: datetime = None, debug: bool = None, cash: float = 0.0):
        '''重置除了fund_code外的数据'''
        if start_time is not None:
            self.start_time = start_time
        if stop_time is not None:
            self.stop_time = stop_time
        if debug is not None:
            self.debug = debug
        self.cash = cash
        self._reset_shares()
        self._reset_queue()
        self._reset_indexes()
        self._reset_record()

    def _log(self, msg: str):
        if self.debug:
            print(f'{self.now.strftime("%Y-%m-%d")} - {msg}')

    def _compute_buy_rate(self, amount: float) -> float:
        for buy_rate in reversed(self.buy_rates):
            if amount >= buy_rate.amount:
                return buy_rate.rate
        return 0

    def _compute_sell_rate(self, buy_time: datetime) -> float:
        holding_time: timedelta = self.now - buy_time
        for sell_rate in reversed(self.sell_rates):
            if holding_time >= sell_rate.holding_time:
                return sell_rate.rate

    def _handle_buy_message(self, msg: Message) -> None:
        if msg.amount > self.cash:
            self._log(f'买入金额不能大于余额{self.cash}')
        amount = min(self.cash, msg.amount)  # 买入amount的钱, 买入金额不大于余额
        if amount < self.min_buy_amount:
            self._log(f'买入金额不能小于{self.min_buy_amount}')
            return
        self.cash -= amount  # 扣除余额
        sub_amount = amount * (1 - self._compute_buy_rate(amount))  # 扣除手续费
        share = sub_amount / self.now_row[NET_WORTH]  # 这amount的钱能换share份额
        self.shares.add(OrderShare(self.now, share,
                        sub_amount, self.now_row[AC_WORTH]))
        self.total_share += share
        self._log(f'买入{amount}元')

    def _handle_sell_message(self, msg: Message) -> None:
        if msg.share > self.total_share:
            self._log(f'卖出份额不能大于持有份额{self.total_share}')
        share = min(self.total_share, msg.share)  # 卖出share份, 卖出份额不大于持有份额
        self.total_share -= share
        amount = 0.0  # 这share份能卖amount的钱
        while len(self.shares) != 0:
            oldest_share: OrderShare = self.shares[0]
            can_sell_share = min(oldest_share.share, share)
            can_sell_amount = oldest_share.amount * \
                (can_sell_share / oldest_share.share)
            add_amount = \
                (1 + (self.now_row[AC_WORTH] - oldest_share.ac_worth) / oldest_share.ac_worth) \
                * can_sell_amount * (1 - self._compute_sell_rate(oldest_share.date))
            # 卖出后得到的金额: (1+增长率) * 可卖出金额 * (1-手续费率)
            amount += add_amount
            share -= can_sell_share
            oldest_share.share -= can_sell_share
            oldest_share.amount -= can_sell_amount
            if abs(oldest_share.share) < 1e-3:
                self.shares.pop(0)
            if abs(share) < 1e-3:
                break
        self.cash += amount
        self._log(f'卖出{amount}份, 卖出{amount}元')

    def _handle_message(self) -> None:
        while len(self.queue) != 0:
            msg: Message = self.queue[0]
            if self.now < msg.date:  # 没到时候处理
                break
            self.queue.pop(0)
            if msg.type == MessageType.BUY:
                self._handle_buy_message(msg)
            elif msg.type == MessageType.SELL:
                self._handle_sell_message(msg)

    def compute_value(self) -> float:
        return self.total_share * self.now_row[AC_WORTH] + self.cash

    def buy(self, amount: float = 0.0):
        buy_msg = Message(
            self.now + timedelta(days=self.T), MessageType.BUY)
        buy_msg.amount = amount
        self.queue.add(buy_msg)
        self._log(f'打算买入{amount}元')

    def sell(self, share: float = 0.0):
        sell_msg = Message(self.now + timedelta(days=self.T), MessageType.SELL)
        sell_msg.share = share
        self.queue.add(sell_msg)
        self._log(f'打算卖出{share}份')

    def execute(self) -> None:
        self.now = self.start_time
        self.before_execute()
        while True:
            self.now_row = get_cur_or_next_date_row(self.now, self.info)
            if self.now_row is None:
                break
            self.now = datetime.strptime(
                str(self.now_row[DATE]), '%Y-%m-%d 00:00:00')
            if self.now > self.stop_time:
                break
            self.before_step()
            # 首先处理消息队列中的消息
            self._handle_message()
            # 执行今天的策略
            self.strategy()
            self.after_step()
            self.now += timedelta(days=1)
        self.after_execute()

    def before_execute(self) -> None:
        '''执行前的准备工作'''
        pass

    def after_execute(self) -> None:
        '''执行后的收尾工作'''
        pass

    def strategy(self) -> None:
        '''策略'''
        pass

    def before_step(self) -> None:
        '''每步执行后的收尾工作'''
        pass

    def after_step(self) -> None:
        '''每步执行前的准备工作'''
        pass
