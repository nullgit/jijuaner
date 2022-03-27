package com.yunzen.jijuaner.company.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
public class CompanyController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello, fund company 12345 !";
    }
}
