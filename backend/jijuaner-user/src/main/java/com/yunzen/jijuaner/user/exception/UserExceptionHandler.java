package com.yunzen.jijuaner.user.exception;

import java.util.Arrays;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.yunzen.fund.user")
public class UserExceptionHandler {
    @ExceptionHandler(value = Exception.class)
    public R handelThrowable(Exception e) {
        e.printStackTrace();
        return R.error().putCode(JiJuanerException.UNKNOWN_EXCEPTION.getCode())
                .putMsg("出现异常：" + e.getMessage());
                // 生产环境下应改为 .putMsg(JiJuanerException.UNKNOWN_EXCEPTION.getMsg());
    }
}
