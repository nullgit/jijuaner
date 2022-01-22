package com.yunzen.jijuaner.fund.entity;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class FundInfoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fundCode;
    private String fundName;
    private String yieldOneYear;
    private String yieldSixMonths;
    private String yieldThreeMonths;
    private String yieldOneMonth;

    // private String acWorthTrend;
    // private String ranksInSimilarType;
    // private String currentManagers;
    // private String scales;

    private List<AcWorth> acWorthTrend;
    private List<RankInSimilarType> ranksInSimilarType;
    private List<CurrentManager> currentManagers;
    private List<Scale> scales;

    @Data
    public static class AcWorth implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long x;
        private String y;
    }

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
