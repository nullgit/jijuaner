package com.yunzen.jijuaner.user.vo;

import java.io.Serializable;

import lombok.Data;

/**
 * 已登录的用户信息
 * <ul>
 * <li>用户id</li>
 * <li>用户名</li>
 * <li>头像链接</li>
 * <li>email</li>
 * </ul>
 */
@Data
public class UserInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer userId;
    private String userName;
    private String email;
    private String headImg;
}
