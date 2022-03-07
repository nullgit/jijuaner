package com.yunzen.jijuaner.user.vo;

import java.io.Serializable;
import java.util.List;

import com.yunzen.jijuaner.common.to.FundSimpleAndRealTimeInfoTo;

import lombok.Data;

@Data
public class UserOptionVo implements Serializable {
    private Integer groupId;
    private Integer userId;
    private String groupName;
    private Short sort;
    private List<String> funds;
    private List<FundSimpleAndRealTimeInfoTo> infos;
}
