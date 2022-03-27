package com.yunzen.jijuaner.fund.config;

import com.yunzen.jijuaner.fund.controller.FundListController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableAsync
@EnableScheduling
public class FundSchedule {
    @Autowired
    FundListController fundListController;

    /**
     * 定时任务，每天凌晨零点更新全部基金列表
     */
    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateAllFundList() {
        fundListController.updateAll();
    }
}
