package com.yunzen.jijuaner.fund.entity;

import lombok.Data;

@Data
public class IndexListEntity {
    private Integer id;
    private String indexCode;
    private String indexName;
    private Double pe;
    private Double pb;
    private Double pePercentile;
    private Double pbPercentile;
    private Double roe;
    private Double yield;
    private Double peg;
    private Boolean pbFlag;
    private String evalType;
}
