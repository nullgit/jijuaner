package com.yunzen.jijuaner.user.exception;

import com.yunzen.jijuaner.common.exception.JiJuanerException;

public class SignInException extends RuntimeException {
    public SignInException() {
        super(JiJuanerException.SIGN_IN_EXCEPTION.getMsg());
    }

    public SignInException(String message) {
        super(message);
    }
}
