package com.yunzen.jijuaner.common.utils;

/**
 * 项目中用到的字符串常量
 */
public enum JiJuanerConstantString {
    VERIFICATION_CODE("jijuaner:code:"),
    INDEX_LIST("jijuaner:indexList"),
    INDEX_LIST_TIME("jijuaner:indexList:time"),
    ALL_OPTION_FUNDS("jijuaner:allOptionFunds:"),
    PAY_TOKEN("jijuaner:payToken:"),
    ;

    private final String constant;

    JiJuanerConstantString(String constant) {
        this.constant = constant;
    }

    public String getConstant() {
        return constant;
    }
}
