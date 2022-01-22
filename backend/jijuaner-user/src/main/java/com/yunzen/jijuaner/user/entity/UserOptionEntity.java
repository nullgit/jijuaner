package com.yunzen.jijuaner.user.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("user_option")
public class UserOptionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "group_id", type = IdType.AUTO)
    private Integer groupId;
    private Integer userId;
    private String groupName;
    private Byte sort;
    private String funds;
}
