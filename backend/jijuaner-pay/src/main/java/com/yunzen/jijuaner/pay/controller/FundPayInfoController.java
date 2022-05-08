package com.yunzen.jijuaner.pay.controller;

import java.math.BigDecimal;

import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.common.utils.SignInUtils;
import com.yunzen.jijuaner.pay.config.PayUtils;
import com.yunzen.jijuaner.pay.entity.FundPayInfoEntity;
import com.yunzen.jijuaner.pay.service.FundPayInfoService;
import com.yunzen.jijuaner.pay.vo.PayFundInfoVo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay/fund")
public class FundPayInfoController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello, jijuaner pay50000!";
    }

    @Autowired
    private FundPayInfoService fundPayInfoService;


    @GetMapping("/updateAllFundPayInfo")
    public R updateAllFundPayInfo() {
        fundPayInfoService.updateAllFundPayInfo();
        return R.ok().putMsg("成功更新全部基金申购信息列表");
    }

    @GetMapping("/info/{fundCode}")
    public R getPayFundInfo(@PathVariable("fundCode") String fundCode) {
        PayFundInfoVo vo = new PayFundInfoVo();
        if (!"暂停申购".equals(vo.getSubscriptionStatus())) {
            vo.setToken(fundPayInfoService.setUserPayToken(SignInUtils.getUserId()));
        }
        FundPayInfoEntity entity = fundPayInfoService.getPayFundInfo(fundCode);
        BeanUtils.copyProperties(entity, vo);
        vo.setMinAmount(PayUtils.changeScale(vo.getMinAmount(), 2));
        vo.setMaxAmountPerDay(PayUtils.changeScale(vo.getMaxAmountPerDay(), 2));
        vo.setServiceCharge(new BigDecimal(entity.getServiceCharge(), 3));
        return R.ok().putData(vo);
    }


}
