package com.yunzen.jijuaner.common.exception;

public class FeignException extends RuntimeException {
    public FeignException() {
        super(JiJuanerException.FEIGN_EXCEPTION.getMsg());
    }

    public FeignException(String message) {
        super(message);
    }
}
