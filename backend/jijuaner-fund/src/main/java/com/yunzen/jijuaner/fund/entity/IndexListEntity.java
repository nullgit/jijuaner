package com.yunzen.jijuaner.fund.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * 指数列表实体类, 保存在 redis
 */
@Data
public class IndexListEntity implements Serializable {
    private static final long serialVersionUID = 1L;

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
