from datetime import datetime

import numpy as np
import pandas as pd
from pyecharts import options as opts
from pyecharts.charts import Line, Page
from pyecharts.globals import ThemeType
from pyecharts.options import TooltipOpts
from utils.back_test import BackTest
from utils.const import AC_WORTH, CASH, DATE, VALUE, NOW


def back_test_dollar_cost_averaging_strategy(fund_code: str):
    tester = DollarCostAveragingBackTest(fund_code)
    tester.cash = 1000000
    tester.start_time = datetime.strptime('2010-01-01', '%Y-%m-%d')
    tester.stop_time = datetime.strptime('2022-07-22', '%Y-%m-%d')
    tester.execute()
    print(tester.records[VALUE][-1])


class DollarCostAveragingBackTest(BackTest):
    def __init__(self, fund_code: str, debug: bool = False) -> None:
        super().__init__(fund_code, debug)

    def before_execute(self) -> None:
        self.records['date'] = []
        self.records['now'] = []

    def after_execute(self) -> None:
        page = Page('回测结果')
        opt = opts.InitOpts(theme=ThemeType.LIGHT, bg_color='white')
        tooltip_opt = TooltipOpts(trigger='axis')
        fund_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.info[DATE].truncate(before=self.start_time).truncate(after=self.stop_time).to_list()) \
            .add_yaxis(AC_WORTH, self.info[AC_WORTH].truncate(before=self.start_time).truncate(after=self.stop_time).to_list())
        record_line = Line(opt).set_global_opts(tooltip_opts=tooltip_opt) \
            .add_xaxis(self.records[DATE]) \
            .add_yaxis(CASH, self.records[CASH]) \
            .add_yaxis(VALUE, self.records[VALUE])
        page.add(fund_line).add(record_line)
        page.render('./output/定投策略回测结果.html')

    def strategy(self) -> None:
        # 每天买入1000元
        self.buy(330)

    def after_step(self) -> None:
        self.records[DATE].append(self.now)
        self.records[CASH].append(self.cash)
        self.records[VALUE].append(self.compute_value())
