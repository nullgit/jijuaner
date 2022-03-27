package com.yunzen.jijuaner.fund.config;

import java.time.LocalDateTime;

import com.yunzen.jijuaner.common.utils.TimeUtils;

import org.springframework.data.redis.core.ValueOperations;

public class FundUtils {
    private FundUtils() {
    }

    /**
     * 如果 redis 中 key 的保存时间已经过了 after, 就返回 null, 否则返回 key 对应的值
     */
    public static String getRedisKeyIfInValidTime(ValueOperations<String, String> opsForValue, String timeKey,
            String key, LocalDateTime after) {
        String timeJSON = opsForValue.get(timeKey);
        if (timeJSON != null) {
            LocalDateTime saveTime = TimeUtils.timeStrampToLocalDateTime(Long.parseLong(timeJSON));
            if (saveTime.isAfter(after)) {
                return opsForValue.get(key);
            }
        }
        return null;
    }

}
