package com.yunzen.jijuaner.fund.feign;

import com.yunzen.jijuaner.common.utils.R;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("jsdata")
public interface JSDataFeignService {
    @RequestMapping("/jsdata/fund/list")
    public R getAllList();

    @RequestMapping("/jsdata/fund/info/{id}")
    public R getInfoById(@PathVariable("id") String id);
}
