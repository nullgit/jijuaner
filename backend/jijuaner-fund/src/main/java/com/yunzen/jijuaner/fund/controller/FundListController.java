package com.yunzen.jijuaner.fund.controller;

import java.util.List;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.FundListEntity;
import com.yunzen.jijuaner.fund.feign.SearchFeignService;
import com.yunzen.jijuaner.fund.service.FundListService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fund/fundList")
public class FundListController {
    @Autowired
    private FundListService fundListService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund!!!";
    }

    @GetMapping("/getAll")
    public R getAll() {
        List<FundListEntity> all = fundListService.getAll();
        return R.ok().putData(all);
    }

    @Autowired
    private SearchFeignService searchFeignService;

    /**
     * 首先将 mongo 中的基金列表更新, 然后将 es 数据库中的基金列表更新
     */
    @GetMapping("/updateAll")
    public R updateAll() {
        fundListService.updateAll();
        R upAllR = searchFeignService.upAll();
        if (upAllR.getCode() == 0) {
            return R.ok().putMsg("成功更新数据库中的基金列表！");
        } else {
            throw JiJuanerException.FEIGN_EXCEPTION.putMessage(upAllR.getMsg());
        }
    }
}
