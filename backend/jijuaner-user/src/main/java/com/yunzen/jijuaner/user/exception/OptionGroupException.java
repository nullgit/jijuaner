package com.yunzen.jijuaner.user.exception;

import com.yunzen.jijuaner.common.exception.JiJuanerException;

public class OptionGroupException extends RuntimeException {
    public OptionGroupException() {
        super(JiJuanerException.OPTION_GROUP_EXCEPTION.getMsg());
    }

    public OptionGroupException(String message) {
        super(message);
    }
}
