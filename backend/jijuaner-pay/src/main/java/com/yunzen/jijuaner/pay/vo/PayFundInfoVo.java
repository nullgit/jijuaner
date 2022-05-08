package com.yunzen.jijuaner.pay.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

import lombok.Data;

/**
 * 带有 token 的基金申购信息
 */
@Data
public class PayFundInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fundCode;
    private String fundName;
    private String fundType;
    private String subscriptionStatus;
    private String redemptionStatus;
    private String nextOpenDay;
    private BigInteger minAmount;
    private BigDecimal serviceCharge;
    private BigInteger maxAmountPerDay;

    private String token;
}
