package com.yunzen.jijuaner.pay.config;

import java.math.BigDecimal;
import java.math.BigInteger;

public class PayUtils {

    private PayUtils() {
    }

    public static BigInteger changeScale(BigInteger i, int scale) {
        return new BigDecimal(i, scale).toBigInteger();
    }

    /**
     * 计算手续费
     * 金额为小数点后2位, 单位为元
     * 手续费小数点后3位, 单位为%
     */
    public static BigInteger countService(BigInteger amount, BigInteger serviceCharge) {
        var pay = new BigDecimal(amount, 2);
        var charge = BigDecimal.ONE.subtract(new BigDecimal(serviceCharge, 5));
        pay = pay.multiply(charge);
        return pay.multiply(BigDecimal.valueOf(100L)).toBigInteger();
    }
}
