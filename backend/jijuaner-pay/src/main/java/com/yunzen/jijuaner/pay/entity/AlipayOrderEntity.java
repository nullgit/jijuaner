package com.yunzen.jijuaner.pay.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("alipay_order")
public class AlipayOrderEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id; // 我的交易号
    private String tradeNo; // 支付宝流水号
    private String buyerId; // 支付者的id
    private String sellerId; // 商家的id
    private BigDecimal buyerPayAmount; // 用户支付的金额
    private BigDecimal totalAmount; // 订单支付的总额
    private BigDecimal receiptAmount; // 商家收到的款
    private BigDecimal invoiceAmount; // 可开发票的金额
    private String subject; // 支付时显示的主题
    private String body; // 支付时显示的订单信息
    private String gmtCreate; // 该笔交易创建的时间
    private String gmtPayment; // 该笔交易支付的时间
    private String gmtClose; // 该笔交易结束的时间
    private String notifyTime; // 通知发送的时间
    private String notifyId; // 通知id
    private String notifyType; // 通知类型； trade_status_sync
    private TradeStatus tradeStatus; // 交易状态 TRADE_SUCCESS
    private String fundBillList; // 支付成功的各个渠道金额信息

    // private String point_amount; // 使用集分宝支付的金额
    // private String version;
    // private String app_id;//应用id
    // private String charset;
    // private String sign;
    // private String sign_type;//签名类型

    public enum TradeStatus {
        WAIT_BUYER_PAY, // 交易创建，等待买家付款。
        TRADE_CLOSED, // 未付款交易超时关闭，或支付完成后全额退款。
        TRADE_SUCCESS, // 交易支付成功。
        TRADE_FINISHED, // 交易结束，不可退款。
    }
}
