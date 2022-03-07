package com.yunzen.jijuaner.common.to;

import java.io.Serializable;

import lombok.Data;

@Data
public class FundSimpleAndRealTimeInfoTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fundCode;
    private String fundName;
    private String netWorth;
    private String valuation;
    private String valuationRate;
    private String valuationTime;
    private String date;

    private String yieldOneYear;
    private String yieldSixMonths;
    private String yieldThreeMonths;
    private String yieldOneMonth;
    private String fundType;
}
