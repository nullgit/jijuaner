package com.yunzen.jijuaner.fund.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FundRealTimeInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fundCode;
    private String fundName;
    private String netWorth;
    private String valuation;
    private String valuationRate;
    private String valuationTime;
    private String date;

    public FundRealTimeInfoEntity(String fundCode) {
        this.fundCode = fundCode;
    }
}
