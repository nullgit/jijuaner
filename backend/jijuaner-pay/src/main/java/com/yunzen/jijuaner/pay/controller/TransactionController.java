package com.yunzen.jijuaner.pay.controller;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.common.utils.SignInUtils;
import com.yunzen.jijuaner.pay.config.AlipayTemplate;
import com.yunzen.jijuaner.pay.config.PayUtils;
import com.yunzen.jijuaner.pay.service.FundPayInfoService;
import com.yunzen.jijuaner.pay.service.TransactionService;
import com.yunzen.jijuaner.pay.vo.AlipayAsyncVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/pay/transaction")
@Slf4j
public class TransactionController {
    @RequestMapping("/hello")
    public String hello() {

        return "hello, pay transaction!!!";
    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AlipayTemplate alipay;

    /**
     * 申购基金
     *
     * @return 返回支付网页
     * @throws AlipayApiException
     */
    @GetMapping(value = "/subscribe", produces = "text/html")
    public String subscribe(
            @RequestParam("token") String token,
            @RequestParam("fundCode") String fundCode,
            @RequestParam("amount") String amount)
            throws AlipayApiException {
        String h5 = transactionService.subscribe(SignInUtils.getUserId(), fundCode, token, amount);
        return h5;
    }

    @PostMapping("/redeem")
    public R redeem(@RequestBody R entity) {
        transactionService.redeem();
        return entity;
    }

    /**
     * listenser
     * 支付宝异步通知回调函数
     */
    @PostMapping("/alipayNotify")
    public String alipayNotify(AlipayAsyncVo vo, HttpServletRequest request) throws AlipayApiException {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        // 验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipay.getAlipayPublicKey(),
                alipay.getCharset(), alipay.getSignType());
        if (signVerified) {
            // 签名校验成功, 去修改交易状态
            transactionService.handlePayResult(vo);
            log.info("交易成功");
            // 返回 success 支付宝便不再通知
            return "success";
        } else {
            log.error("签名验证失败");
            // 返回非 success, 支付宝会在一天内的特定时间间隔多次重发通知, 4m,10m,10m,1h,2h,6h,15h
            return "error";
        }
    }

    @Autowired
    private FundPayInfoService fundPayInfoService;

    @GetMapping("/test/{fundCode}")
    public R test(@PathVariable("fundCode") String fundCode) {
        return R.ok().putData(PayUtils.countService(new BigInteger("10000"), new BigInteger("150")));
    }
}
