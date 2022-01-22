package com.yunzen.jijuaner.user.exception;

import com.yunzen.jijuaner.common.exception.JiJuanerException;

public class LoginException extends RuntimeException {
    public LoginException() {
        super(JiJuanerException.LOGIN_EXCEPTION.getMsg());
    }

    public LoginException(String message) {
        super(message);
    }
}
