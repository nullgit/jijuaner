package com.yunzen.jijuaner.pay.config;

import java.math.BigDecimal;

public class PayUtils {
    public static BigDecimal setScale(BigDecimal num, int scale) {
        int base = 1;
        for (int i = 0; i < scale; ++i) {
            base *= 10;
        }
        BigDecimal divide = new BigDecimal( Integer.toString(base));
        return new BigDecimal(num.multiply(divide).toBigInteger()).divide(divide);
    }
}
