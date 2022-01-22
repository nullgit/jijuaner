package com.yunzen.jijuaner.fund.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("fund_list")
public class FundListEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "fund_code")
    private String fundCode;
    private String fundName;
    private String fundType;

}
