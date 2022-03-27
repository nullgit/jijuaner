package com.yunzen.jijuaner.pay.service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.pay.entity.FundPayInfoEntity;
import com.yunzen.jijuaner.pay.feign.JSDataFeignService;
import com.yunzen.jijuaner.pay.vo.SubscribeVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("payService")
@Slf4j
public class PayService {
    @Autowired
    private MongoTemplate mongo;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private JSDataFeignService jsDataFeignService;

    public FundPayInfoEntity getPayFundInfo(String fundCode) {
        return mongo.findById(fundCode, FundPayInfoEntity.class);
    }

    public void updateAllFundPayInfo() {
        R allListR = jsDataFeignService.getAllFundPayInfo();
        if (allListR.getCode() == 0) {
            List<FundPayInfoEntity> allList = JSON.parseObject(JSON.toJSONString(allListR.getData()),
                    new TypeReference<List<FundPayInfoEntity>>() {
                    });
            log.info("开始基金申购信息列表数据，共{}条", allList.size());
            mongo.dropCollection(FundPayInfoEntity.class);
            mongo.insert(allList, FundPayInfoEntity.class);
            log.info("更新基金申购信息列表数据，共{}条", allList.size());
        } else {
            throw JiJuanerException.FEIGN_EXCEPTION;
        }
    }

    public String setUserPayToken(Integer userId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        redis.opsForValue().set(JiJuanerConstantString.PAY_TOKEN.getConstant() + userId.toString(), token, 10,
                TimeUnit.MINUTES);
        return token;
    }

    public void subscribe(SubscribeVo vo) {
        // TODO 首先检查 token 是否正确, 然后向数据库中插入一条申购记录, 定时(交易日下午3:00后)将申购记录发送给基金公司
    }
}
