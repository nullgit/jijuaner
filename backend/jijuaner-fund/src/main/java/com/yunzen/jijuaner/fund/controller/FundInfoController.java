package com.yunzen.jijuaner.fund.controller;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.FundInfoEntity;
import com.yunzen.jijuaner.fund.exception.FundInfoException;
import com.yunzen.jijuaner.fund.service.FundInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fund/fundInfo")
public class FundInfoController {
    @Autowired
    private FundInfoService fundInfoService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund info!!!";
    }

    @RequestMapping("/{id}")
    public R getInfoById(@PathVariable("id") String id) {
        try {
            FundInfoEntity info = fundInfoService.getInfoById(id);
            return R.ok().putData(info);
        } catch (FundInfoException e) {
            return R.error().putCode(JiJuanerException.FUND_INFO_EXCEPTION.getCode()).putMsg(e.getMessage());
        }
    }
}
