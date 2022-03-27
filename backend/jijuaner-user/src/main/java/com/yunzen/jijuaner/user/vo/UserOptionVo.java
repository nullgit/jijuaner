package com.yunzen.jijuaner.user.vo;

import java.io.Serializable;
import java.util.List;

import com.yunzen.jijuaner.common.to.FundSimpleAndRealTimeInfoTo;

import lombok.Data;

/**
 * 向用户返回的自选分组信息, 与实体类不同的地方在于: funds 使用 List 表示, 并加上了简单信息和实时数据
 */
@Data
public class UserOptionVo implements Serializable {
    private Integer groupId;
    private Integer userId;
    private String groupName;
    private Short sort;
    private List<String> funds;
    private List<FundSimpleAndRealTimeInfoTo> infos;
}
