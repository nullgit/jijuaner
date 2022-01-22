package com.yunzen.jijuaner.common.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class R implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String msg;
    private Object data;

    public static R ok() {
        R r = new R();
        r.setCode(0);
        return r;
    }

    public static R error() {
        R r = new R();
        r.setCode(-1);
        return r;
    }

    public R putData(Object data) {
        this.setData(data);
        return this;
    }

    public R putCode(Integer code) {
        this.setCode(code);
        return this;
    }

    public R putMsg(String Msg) {
        this.setMsg(Msg);
        return this;
    }
}
