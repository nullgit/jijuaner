package com.yunzen.jijuaner.fund.controller;

import java.util.List;

import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.IndexListEntity;
import com.yunzen.jijuaner.fund.service.IndexListService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fund/indexList")
public class IndexListController {
    @Autowired
    private IndexListService indexListService;

    @RequestMapping("/hello")
    public String hello() {
        return "hello, indexList!!!";
    }

    @GetMapping("/all")
    public R all() {
        List<IndexListEntity> all = indexListService.getAll();
        return R.ok().putData(all);
    }
}
