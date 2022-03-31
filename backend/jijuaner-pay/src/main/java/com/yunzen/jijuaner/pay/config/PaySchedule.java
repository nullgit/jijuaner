package com.yunzen.jijuaner.pay.config;

import com.yunzen.jijuaner.pay.controller.FundPayInfoController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableAsync
@EnableScheduling
public class PaySchedule {
    @Autowired
    FundPayInfoController fundPayInfoController;

    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    /**
     * 定时任务，每天凌晨零点更新全部基金申购信息列表
     */
    public void updateAllFundPayInfo() {
        fundPayInfoController.updateAllFundPayInfo();
    }

    
}
