package com.yunzen.jijuaner.fund.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "fund_list")
public class FundListEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String fundCode;
    private String fundName;
    private String fundType;
}
