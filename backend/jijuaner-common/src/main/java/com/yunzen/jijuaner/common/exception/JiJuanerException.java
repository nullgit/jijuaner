package com.yunzen.jijuaner.common.exception;

/**
 * @author:Ryan
 * @time:30
 * @description：自定义异常类，向 View 展示
 *                       9xxxx: 通用异常
 *                       1xxxx: fund 服务异常
 *                       2xxxx: user 服务异常
 *                       3xxxx: search 服务异常
 *                       4xxxx: jsdata 服务异常
 */

public enum JiJuanerException {
    UNKNOWN_EXCEPTION(90000, "未知异常，请联系管理员"),
    INTERNAL_EXCEPTION(90001, "服务器内部错误"),
    VALID_EXCEPTION(90002, "数据校验异常"),
    FEIGN_EXCEPTION(90003, "feign远程调用异常"),

    FUND_INFO_EXCEPTION(10001, "基金数据出现异常"),

    LOGIN_EXCEPTION(20001, "注册异常"),
    SIGN_IN_EXCEPTION(20002, "登录异常"),
    OPTION_GROUP_EXCEPTION(20003, "自选分组异常"),
    ;

    private final Integer code;
    private final String msg;

    JiJuanerException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
