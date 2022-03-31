package com.yunzen.jijuaner.pay.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 向支付宝提供的支付订单信息
 */
@Data
public class AliPayVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String outTradeNo; // 商户订单号 必填
    private String subject; // 订单名称 必填
    private String totalAmount; // 付款金额 必填
    private String body; // 商品描述 选填
}
