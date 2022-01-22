package com.yunzen.jijuaner.user.feign;

import java.util.List;

import com.yunzen.jijuaner.common.utils.R;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("fund")
public interface FundFeignService {
    @RequestMapping("/fund/fundList/getNames")
    public R getNames(@RequestBody List<String> fundCodes);
}
