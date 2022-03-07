package com.yunzen.jijuaner.common.to;

import java.io.Serializable;

import lombok.Data;

@Data
public class FundListTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fundCode;
    private String fundName;
    private String fundType;
}
