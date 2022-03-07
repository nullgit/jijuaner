package com.yunzen.jijuaner.fund.service;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.yunzen.jijuaner.common.exception.FeignException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.FundInfoEntity;
import com.yunzen.jijuaner.fund.entity.FundRealTimeInfoEntity;
import com.yunzen.jijuaner.fund.exception.FundInfoException;
import com.yunzen.jijuaner.fund.feign.JSDataFeignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("fundInfoService")
public class FundInfoService {
    @Autowired
    private JSDataFeignService jsDataFeignService;

    @Autowired
    private FundListService fundListService;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final String _ID = "_id";

    public void saveInfoById(String id) throws FundInfoException, FeignException {
        R infoResp = jsDataFeignService.getInfoById(id);
        if (infoResp.getCode() == 0) {
            FundInfoEntity fundInfo = JSON.parseObject(JSON.toJSONString(infoResp.getData()), FundInfoEntity.class);
            fundInfo.setFundType(fundListService.getFundType(id));
            fundInfo.setSaveTime(System.currentTimeMillis());
            mongoTemplate.save(fundInfo);
        } else if (infoResp.getCode() == 10001) {
            // 不存在该基金或该接口出错了
            throw new FundInfoException(infoResp.getMsg());
        } else {
            throw new FeignException();
        }
    }

    @Cacheable(cacheNames = "jijuaner:fundInfo")
    public FundInfoEntity getInfoById(String id) throws FundInfoException {
        FundInfoEntity findById = mongoTemplate.findById(id, FundInfoEntity.class);
        if (findById != null && System.currentTimeMillis() - findById.getSaveTime() < 6 * 60 * 1000) {
            // 6 小时内的数据仍可以使用
            return findById;
        }
        try {
            // 从接口中获取最新数据并保存在数据库中
            saveInfoById(id);
        } catch (FeignException e) {
            // 虽然远程调用失败，但是还是可以先使用旧的数据
            return findById;
        }
        // 重新从数据库中获取
        return mongoTemplate.findById(id, FundInfoEntity.class);
    }

    @Cacheable(cacheNames = "jijuaner:fundSimpleInfo")
    public FundInfoEntity getSimpleInfoById(String id) {
        var query = Query.query(Criteria.where(_ID).is(id));
        query.fields().exclude("acWorthTrend", "ranksInSimilarType", "currentManagers", "scales");
        FundInfoEntity findOne = mongoTemplate.findOne(query, FundInfoEntity.class);
        if (findOne == null || System.currentTimeMillis() - findOne.getSaveTime() > 6 * 60 * 1000) {
            saveInfoById(id);
            return mongoTemplate.findOne(query, FundInfoEntity.class);
        }
        return findOne;
    }

    public FundRealTimeInfoEntity getRealTimeInfoById(String id) {
        R realTimeInfoR = jsDataFeignService.getRealTimeInfoById(id);
        if (realTimeInfoR.getCode() == 0) {
            return JSON.parseObject(JSON.toJSONString(realTimeInfoR.getData()), FundRealTimeInfoEntity.class);
        } else {
            return new FundRealTimeInfoEntity(id);
        }
    }
}
