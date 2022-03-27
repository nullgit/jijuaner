package com.yunzen.jijuaner.user.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 用户列表实体类, 保存在 MySQL 中
 * <ul>
 * <li>用户id</li>
 * <li>用户名</li>
 * <li>email</li>
 * <li>用户头像链接</li>
 * <li>加密密码</li>
 * </ul>
 */
@Data
@TableName("user_list")
public class UserListEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "user_id", type = IdType.AUTO)
    private Integer userId;
    private String userName;
    private String email;
    private String password;
    private String headImg;
}
