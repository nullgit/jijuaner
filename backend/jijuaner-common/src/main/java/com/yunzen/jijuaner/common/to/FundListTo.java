package com.yunzen.jijuaner.common.to;

import java.io.Serializable;

import lombok.Data;

/**
 * 基金列表信息, fund 微服务向 search 微服务传输的传输对象, 用来同步 search 微服务中 es 数据库的基金列表
 */
@Data
public class FundListTo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fundCode;
    private String fundName;
    private String fundType;
}
