package com.yunzen.jijuaner.common.utils;

import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.interceptor.UserInterceptor;
import com.yunzen.jijuaner.common.to.UserInfoTo;

/**
 * 登录工具类, 主要用来获取 session
 */
public class SignInUtils {
    private SignInUtils() {
    }

    public static UserInfoTo getUserInfoTo() throws JiJuanerException {
        try {
            return UserInterceptor.toThreadLocal.get();
        } catch (Exception e) {
            throw JiJuanerException.SIGN_IN_EXCEPTION;
        }
    }

    public static Integer getUserId() throws JiJuanerException {
        try {
            return UserInterceptor.toThreadLocal.get().getUserId();
        } catch (Exception e) {
            throw JiJuanerException.SIGN_IN_EXCEPTION;
        }
    }
}
