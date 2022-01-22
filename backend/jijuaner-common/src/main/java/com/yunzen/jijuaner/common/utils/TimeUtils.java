package com.yunzen.jijuaner.common.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TimeUtils {
    private TimeUtils() {

    }
    
    public static LocalDateTime timeStrampToLocalDateTime(long timeStramp) {
        return Instant.ofEpochMilli(timeStramp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
    }
}
