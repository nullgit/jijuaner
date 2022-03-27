package com.yunzen.jijuaner.pay.controller;

import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.common.utils.SignInUtils;
import com.yunzen.jijuaner.pay.entity.FundPayInfoEntity;
import com.yunzen.jijuaner.pay.service.PayService;
import com.yunzen.jijuaner.pay.vo.PayFundInfoVo;
import com.yunzen.jijuaner.pay.vo.SubscribeVo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello, jijuaner pay50000!";
    }

    @Autowired
    private PayService payService;

    @GetMapping("/updateAllFundPayInfo")
    public R updateAllFundPayInfo() {
        payService.updateAllFundPayInfo();
        return R.ok().putMsg("成功更新全部基金申购信息列表");
    }

    @GetMapping("/fundInfo/{fundCode}")
    public R getPayFundInfo(@PathVariable("fundCode") String fundCode) {
        PayFundInfoVo vo = new PayFundInfoVo();
        vo.setToken(payService.setUserPayToken(SignInUtils.getUserId()));
        FundPayInfoEntity entity = payService.getPayFundInfo(fundCode);
        BeanUtils.copyProperties(entity, vo);
        return R.ok().putData(vo);
    }

    @PostMapping("/subscribe")
    public R subscribe(@RequestBody SubscribeVo vo) {
        vo.setUserId(SignInUtils.getUserId());
        payService.subscribe(vo);
        return R.ok();
    }
}
