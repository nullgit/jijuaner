package com.yunzen.jijuaner.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 项目中会用到的时间工具包
 */
public class TimeUtils {
    private TimeUtils() {
    }

    /**
     * 将时间戳转为 LocalDateTime 对象
     */
    public static LocalDateTime timeStrampToLocalDateTime(long timeStramp) {
        return Instant.ofEpochMilli(timeStramp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
    }
}
