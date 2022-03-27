package com.yunzen.jijuaner.common.utils;

import lombok.Data;

import java.io.Serializable;

import com.yunzen.jijuaner.common.exception.JiJuanerException;

/**
 * 自定义前后端数据传输对象
 */
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

    public static R error(JiJuanerException e) {
        R r = new R();
        r.setCode(e.getCode());
        r.setMsg(e.getMessage());
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

    public R putMsg(String msg) {
        this.setMsg(msg);
        return this;
    }
}
