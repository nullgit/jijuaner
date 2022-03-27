package com.yunzen.jijuaner.fund.service;

import java.time.LocalDateTime;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.config.FundUtils;
import com.yunzen.jijuaner.fund.entity.IndexListEntity;
import com.yunzen.jijuaner.fund.feign.JSDataFeignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("indexListService")
@Slf4j
public class IndexListService {
    @Autowired
    private JSDataFeignService jsDataFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<IndexListEntity> getAll() {
        ValueOperations<String, String> opsForValue = redisTemplate.opsForValue();
        String timeKey = JiJuanerConstantString.INDEX_LIST_TIME.getConstant();
        String key = JiJuanerConstantString.INDEX_LIST.getConstant();
        String resultJSON = FundUtils.getRedisKeyIfInValidTime(opsForValue, timeKey, key,
                LocalDateTime.now().minusHours(12));
        var typeReference = new TypeReference<List<IndexListEntity>>() {
        };
        if (resultJSON != null) {  // redis 中的数据没有过期
            return JSON.parseObject(resultJSON, typeReference);
        }
        R allIndexR = jsDataFeignService.getAllIndex();
        if (allIndexR.getCode() == 0) {
            resultJSON = JSON.toJSONString(allIndexR.getData());
            List<IndexListEntity> allIndex = JSON.parseObject(resultJSON, typeReference);
            opsForValue.set(key, resultJSON);
            opsForValue.set(timeKey, Long.toString(System.currentTimeMillis()));
            return allIndex;
        } else {
            // 虽然远程调用失败，但是还是可以先使用旧的数据
            log.error(allIndexR.getMsg());
            return JSON.parseObject(opsForValue.get(key), typeReference);
        }
    }

}
