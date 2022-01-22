package com.yunzen.jijuaner.fund.controller;

import java.util.ArrayList;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.FundInfoEntity;
import com.yunzen.jijuaner.fund.entity.FundListEntity;
import com.yunzen.jijuaner.fund.exception.FundInfoException;
import com.yunzen.jijuaner.fund.service.FundInfoService;
import com.yunzen.jijuaner.fund.service.FundListService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fund/fundList")
@CacheConfig(cacheNames = "jijuaner:fundList")
public class FundListController {
    @Autowired
    private FundListService fundListService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund!!!";
    }

    @RequestMapping("/getAll")
    // @Caching(evict = {
    // @CacheEvict(value = {"fundList"}, key = "'getAll'")
    // })
    @Cacheable(key = "'getAll'")
    public R getAll() {
        List<FundListEntity> all = fundListService.list();
        return R.ok().putData(all);
    }

    @RequestMapping("/getPage")
    public R getPage(@RequestParam Long current, @RequestParam Long size) {
        Page<FundListEntity> page = fundListService.page(new Page<>(current, size));
        return R.ok().putData(page);
    }

    @RequestMapping("/updateAll")
    public R updateAll() {
        // TODO 定时任务，并在 search 中 upAll
        fundListService.updateAll();
        return R.ok().putMsg("成功更新数据库中的基金列表！");
    }

    @RequestMapping("/getNames")
    public R getNames(@RequestBody List<String> fundCodes) {
        if (fundCodes.isEmpty()) {
            return R.ok().putData(new ArrayList<String>());
        }
        List<String> fundNames = fundCodes.stream().map(fundCode -> fundListService.getFundName(fundCode)).toList();
        if (fundCodes.size() != fundNames.size()) {
            R.error().putMsg("列表中有些基金不存在或重复");
        }
        return R.ok().putData(fundNames);
    }
}
