package com.yunzen.jijuaner.common.utils;

public enum JiJuanerConstantString {
    VERIFICATION_CODE("jijuaner:code:"),
    INDEX_LIST("jijuaner:indexList"),
    INDEX_LIST_TIME("jijuaner:indexList:time"),
    ALL_OPTION_FUNDS("jijuaner:allOptionFunds:"),
    ;

    private final String constant;

    JiJuanerConstantString(String constant) {
        this.constant = constant;
    }

    public String getConstant() {
        return constant;
    }
}
