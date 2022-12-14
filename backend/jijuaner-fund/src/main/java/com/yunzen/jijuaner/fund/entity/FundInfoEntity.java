package com.yunzen.jijuaner.fund.entity;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * 基金信息实体类, 保存在 MongoDB
 */
@Data
@Document(collection = "fund_info")
public class FundInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String fundCode;
    private String fundName;
    private String yieldOneYear;
    private String yieldSixMonths;
    private String yieldThreeMonths;
    private String yieldOneMonth;

    private String fundType;
    private Long saveTime;

    private List<Long> x; // 时间戳
    private List<String> netWorthTrend; // 单位累计净值
    private List<String> acWorthTrend; // 复权累计净值

    private List<RankInSimilarType> ranksInSimilarType;
    private List<CurrentManager> currentManagers;
    private List<Scale> scales;

    // @Data
    // public static class Worth implements Serializable {
    //     private static final long serialVersionUID = 1L;

    //     private Long x;
    //     private String y;
    // }

    @Data
    public static class RankInSimilarType implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long x;
        private Integer y;
        private Integer total;
    }

    @Data
    public static class CurrentManager implements Serializable {
        private static final long serialVersionUID = 1L;

        private String managerId;
        private String pic;
        private String name;
        private String workTime;
        private String fundSize;
    }

    @Data
    public static class Scale implements Serializable {
        private static final long serialVersionUID = 1L;

        private String x;
        private String y;
    }
}
