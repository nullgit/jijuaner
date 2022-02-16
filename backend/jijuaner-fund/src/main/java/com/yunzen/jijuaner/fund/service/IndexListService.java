package com.yunzen.jijuaner.fund.service;

import java.time.LocalDateTime;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.exception.FeignException;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.config.FundUtils;
import com.yunzen.jijuaner.fund.entity.IndexListEntity;
import com.yunzen.jijuaner.fund.feign.JSDataFeignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service("indexListService")
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
        if (resultJSON != null) {
            return JSON.parseObject(resultJSON, new TypeReference<List<IndexListEntity>>() {
            });
        }
        R allIndexR = jsDataFeignService.getAllIndex();
        if (allIndexR.getCode() == 0) {
            resultJSON = JSON.toJSONString(allIndexR.getData());
            List<IndexListEntity> allIndex = JSON.parseObject(resultJSON,
                    new TypeReference<List<IndexListEntity>>() {
                    });
            opsForValue.set(key, resultJSON);
            opsForValue.set(timeKey, Long.toString(System.currentTimeMillis()));
            return allIndex;
        } else {
            // TODO 虽然远程调用失败，但是还是可以先使用旧的数据
            throw new FeignException(allIndexR.getMsg());
        }
    }

}
