package com.yunzen.jijuaner.user.feign;

import java.util.List;

import com.yunzen.jijuaner.common.utils.R;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("fund")
public interface FundFeignService {
    @PostMapping("/fund/fundInfo/simpleAndRealTime")
    public R getSimpleAndRealTimeInfos(@RequestBody List<String> ids);
}
