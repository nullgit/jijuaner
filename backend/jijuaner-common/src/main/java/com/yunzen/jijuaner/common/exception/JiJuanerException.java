package com.yunzen.jijuaner.common.exception;

/**
 * 项目自定义异常类, 添加了异常状态码, 其常量定义了项目中常用的自定义异常类
 * 自定义异常类，向 View 展示
 * <ul>
 * <li>1xxxx: fund 服务异常</li>
 * <li>9xxxx: 通用异常</li>
 * <li>2xxxx: user 服务异常</li>
 * <li>3xxxx: search 服务异常</li>
 * <li>33xxx: jsdata 服务异常</li>
 * <li>4xxxx: comment 服务异常</li>
 * <li>5xxxx: pay 服务异常</li>
 * </ul>
 *
 * @field code 异常状态码
 */
public class JiJuanerException extends RuntimeException {
    private final Integer code;

    public static final JiJuanerException UNKNOWN_EXCEPTION = new JiJuanerException(-1, "未知异常，请联系管理员");
    public static final JiJuanerException INTERNAL_EXCEPTION = new JiJuanerException(90001, "服务器内部错误");
    public static final JiJuanerException VALID_EXCEPTION = new JiJuanerException(90002, "数据校验异常");
    public static final JiJuanerException FEIGN_EXCEPTION = new JiJuanerException(90003, "feign远程调用异常");

    public static final JiJuanerException FUND_INFO_EXCEPTION = new JiJuanerException(10001, "基金数据出现异常");

    public static final JiJuanerException LOGIN_EXCEPTION = new JiJuanerException(20001, "注册异常");
    public static final JiJuanerException SIGN_IN_EXCEPTION = new JiJuanerException(20002, "登录异常");
    public static final JiJuanerException OPTION_EXCEPTION = new JiJuanerException(20003, "自选分组异常");

    public static final JiJuanerException UP_FUND_EXCEPTION = new JiJuanerException(30001, "上架基金异常");

    public static final JiJuanerException SUBSCRIBE_EXCEPTION = new JiJuanerException(50001, "申购异常");

    /**
     * 已有的自定义异常状态码 + 异常消息
     */
    public JiJuanerException(JiJuanerException e, String message) {
        super(message);
        code = e.getCode();
    }

    /**
     * 默认构造自定义未知异常
     */
    public JiJuanerException() {
        super(JiJuanerException.UNKNOWN_EXCEPTION.getMessage());
        code = JiJuanerException.UNKNOWN_EXCEPTION.getCode();
    }

    /**
     * 带了异常消息的自定义未知异常
     */
    public JiJuanerException(String message) {
        super(message);
        code = JiJuanerException.UNKNOWN_EXCEPTION.getCode();
    }

    /**
     * 带了异常状态码和异常消息的自定义异常
     */
    public JiJuanerException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    /**
     * 替换原来的异常消息, 并新建一个异常对象返回
     * <p>
     * 常用于替换常量异常的消息, 如:
     *
     * <pre>
     * throw JiJuanerException.VALID_EXCEPTION.putMessage(msg)
     * </pre>
     */
    public JiJuanerException putMessage(String message) {
        return new JiJuanerException(code, message);
    }
}
