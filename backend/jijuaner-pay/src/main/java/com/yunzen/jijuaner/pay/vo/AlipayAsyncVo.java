package com.yunzen.jijuaner.pay.vo;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class AlipayAsyncVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gmt_create;
    private String charset;
    private String gmt_payment;
    private String gmt_close;
    private String notify_time;
    private String subject;
    private String sign;
    private String buyer_id;//支付者的id
    private String body;//订单的信息
    private String invoice_amount;//支付金额
    private String version;// 调用的接口版本，固定为：1.0
    private String notify_id;//通知id
    private String fund_bill_list; // 支付成功的各个渠道金额信息
    private String notify_type;//通知类型； trade_status_sync
    private String out_trade_no;//订单号
    private String total_amount;//支付的总额
    private String trade_status;//交易状态  TRADE_SUCCESS
    private String trade_no;//流水号
    private String receipt_amount;//商家收到的款
    private String point_amount;//
    private String app_id;//应用id
    private String buyer_pay_amount;//最终支付的金额
    private String sign_type;//签名类型
    private String seller_id;//商家的id
}

