package com.yunzen.jijuaner.fund.service;

import java.time.LocalDateTime;
import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.exception.FeignException;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.common.utils.TimeUtils;
import com.yunzen.jijuaner.fund.entity.FundInfoEntity;
import com.yunzen.jijuaner.fund.exception.FundInfoException;
import com.yunzen.jijuaner.fund.feign.JSDataFeignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("fundInfoService")
@CacheConfig(cacheNames = "jijuaner:fundInfo")
// JiJuanerConstantString.FUNDINFO.getConstant()
@Slf4j
public class FundInfoService {
    @Autowired
    private JSDataFeignService jsDataFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public FundInfoEntity getInfoById(String id) throws FundInfoException, FeignException {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String timeKey = JiJuanerConstantString.FUND_INFO_TIME.getConstant() + id;
        String key = JiJuanerConstantString.FUND_INFO.getConstant() + id;

        String timeJSON = opsForValue.get(timeKey);
        if (timeJSON != null) {
            LocalDateTime saveTime = TimeUtils.timeStrampToLocalDateTime(Long.parseLong(timeJSON));
            if (saveTime.isAfter(LocalDateTime.now().minusHours(6))) {
                // 如果是 6 小时内的数据，直接返回
                String resultJSON = opsForValue.get(key);
                return JSON.parseObject(resultJSON, FundInfoEntity.class);
            }
        }
        R infoResp = jsDataFeignService.getInfoById(id);
        if (infoResp.getCode() == 0) {
            String resultJSON = (String) infoResp.getData();
            FundInfoEntity fundInfo = JSON.parseObject(resultJSON, FundInfoEntity.class);
            opsForValue.set(key, resultJSON);
            opsForValue.set(timeKey, Long.toString(System.currentTimeMillis()));
            return fundInfo;
        } else if (infoResp.getCode() == 10001) {
            throw new FundInfoException(infoResp.getMsg());
        } else {
            // TODO 虽然远程调用失败，但是还是可以先使用旧的数据
            throw new FeignException();
        }
    }

}
