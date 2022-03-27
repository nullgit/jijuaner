package com.yunzen.jijuaner.common.to;

import java.io.Serializable;

import lombok.Data;

/**
 * 用户 session 的数据
 */
@Data
public class UserInfoTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
}
