package com.yunzen.fund.search.controller;

import java.io.IOException;
import java.util.List;

import com.yunzen.fund.search.service.SearchService;
import com.yunzen.jijuaner.common.to.FundListTo;
import com.yunzen.jijuaner.common.utils.R;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchContoller {
    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund serach!";
    }

    @Autowired
    private SearchService searchService;

    @RequestMapping("/upAll")
    public R upAll() throws IOException {
        boolean successful = searchService.upAll();
        if (successful) {
            return R.ok().putMsg("上架所有基金成功");
        } else {
            return R.error().putMsg("上架所有基金失败");
        }
    }

    @RequestMapping("/search")
    public R search(@RequestParam("input") String input) throws IOException {
        if (input == null || input.length() == 0) {
            return R.error().putMsg("请输入基金名称或代码！");
        }
        List<FundListTo> list = searchService.search(input);
        return R.ok().putData(list);
    }
}
