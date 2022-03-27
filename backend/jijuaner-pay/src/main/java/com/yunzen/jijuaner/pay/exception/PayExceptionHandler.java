package com.yunzen.jijuaner.pay.exception;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理类, 若是自定义异常, 则返回具体信息, 若是其他异常, 则返回未知异常
 * <p>
 * 标准配置
 */
@RestControllerAdvice(basePackages = "com.yunzen.jijuaner.pay")
public class PayExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public R handelThrowable(Exception e) {
        e.printStackTrace();
        if (e instanceof JiJuanerException jiJuanerException) {
            return R.error(jiJuanerException);
        } else {
            return R.error(JiJuanerException.UNKNOWN_EXCEPTION);
        }
    }
}
