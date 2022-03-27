package com.yunzen.jijuaner.fund.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 基金实时数据, 包括基金代码, 基金名称, 单位净值, 累计净值, 实时估值, 实时估计涨跌幅, 估值日期
 * <p>
 * 数据从接口从获得, 并非从数据库中获得
 */
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
