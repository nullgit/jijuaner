package com.yunzen.fund.search.feign;

import com.yunzen.jijuaner.common.utils.R;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("fund")
public interface FundFeignService {
    @RequestMapping("/fund/fundList/getAll")
    public R getAllList();
}
