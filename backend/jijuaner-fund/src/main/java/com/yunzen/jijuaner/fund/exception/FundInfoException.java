package com.yunzen.jijuaner.fund.exception;

import com.yunzen.jijuaner.common.exception.JiJuanerException;

public class FundInfoException extends RuntimeException {
    public FundInfoException() {
        super(JiJuanerException.FUND_INFO_EXCEPTION.getMsg());
    }

    public FundInfoException(String message) {
        super(message);
    }
}
