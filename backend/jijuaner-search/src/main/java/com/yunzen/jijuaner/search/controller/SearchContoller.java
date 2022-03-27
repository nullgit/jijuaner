package com.yunzen.jijuaner.search.controller;

import java.io.IOException;
import java.util.List;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.to.FundListTo;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.search.service.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchContoller {
    @RequestMapping("/hello")
    public String hello() {
        return "hello, search!";
    }

    @Autowired
    private SearchService searchService;

    @GetMapping("/upAll")
    public R upAll() throws IOException {
        searchService.upAll();
        return R.ok().putMsg("上架所有基金成功");
    }

    @GetMapping("/search")
    public R search(@RequestParam("input") String input) throws IOException {
        if (!StringUtils.hasText(input)) {
            throw JiJuanerException.VALID_EXCEPTION.putMessage("请输入基金名称或代码");
        }
        List<FundListTo> list = searchService.search(input);
        return R.ok().putData(list);
    }
}
