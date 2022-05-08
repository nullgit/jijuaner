package com.yunzen.jijuaner.pay.entity;

import java.io.Serializable;
import java.math.BigInteger;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "fund_pay_info")
public class FundPayInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String fundCode;
    private String fundName;
    private String fundType;
    private String subscriptionStatus;
    private String redemptionStatus;
    private String nextOpenDay;
    private BigInteger minAmount;
    private BigInteger maxAmountPerDay;
    private BigInteger serviceCharge;
}
