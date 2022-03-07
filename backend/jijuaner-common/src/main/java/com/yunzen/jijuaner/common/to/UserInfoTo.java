package com.yunzen.jijuaner.common.to;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserInfoTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String userName;
    private String email;
    private String headImg;
}
