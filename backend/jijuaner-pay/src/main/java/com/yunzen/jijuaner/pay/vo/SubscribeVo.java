package com.yunzen.jijuaner.pay.vo;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

/**
 * 申购基金时, 用户填写的表单
 */
@Data
public class SubscribeVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String fundCode;
    private BigDecimal amount;
    private String token;
}
