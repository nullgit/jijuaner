package com.yunzen.jijuaner.pay.entity;

import java.io.Serializable;
import java.math.BigInteger;

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
@TableName("transaction")
public class TransactionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    // 使用雪花算法生成交易记录的ID
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Integer userId;
    private TransactionType type;
    private String fundCode;
    private BigInteger amount;
    private Long time;

    public enum TransactionType {
        UNPAYED_SUBSCRIBE,
        PAYED_SUBSCRIBE,
        REDEEM,
        CANCEL_SUBSCRIBE,
        CANCEL_REDEEM,
        SUBSCRIBE_DONE,
        SUBSCRIBE_TIMEOUT,
        REDEEM_DONE,
    }
}
