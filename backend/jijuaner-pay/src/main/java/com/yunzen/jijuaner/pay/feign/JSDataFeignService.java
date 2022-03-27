package com.yunzen.jijuaner.pay.feign;

import com.yunzen.jijuaner.common.utils.R;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("jsdata")
public interface JSDataFeignService {
    @RequestMapping("/jsdata/fund/subscriptionStatus")
    public R getAllFundPayInfo();
}
