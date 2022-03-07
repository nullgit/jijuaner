package com.yunzen.jijuaner.fund.feign;

import java.io.IOException;

import com.yunzen.jijuaner.common.utils.R;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("search")
public interface SearchFeignService {
    @RequestMapping("/search/upAll")
    public R upAll();
}
