package com.yunzen.jijuaner.fund.service;

import com.alibaba.fastjson.JSON;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.FundInfoEntity;
import com.yunzen.jijuaner.fund.entity.FundRealTimeInfoEntity;
import com.yunzen.jijuaner.fund.feign.JSDataFeignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service("fundInfoService")
public class FundInfoService {
    @Autowired
    private JSDataFeignService jsDataFeignService;

    @Autowired
    private FundListService fundListService;

    @Autowired
    private MongoTemplate mongo;

    private static final String _ID = "_id";

    /**
     * 向 mongo 中保存最新的基金具体信息数据
     */
    public void saveInfoById(String id) {
        // TODO 加上分布式锁, 相同的 id 只有一个线程能从接口获取数据并写入数据库中
        R infoResp = jsDataFeignService.getInfoById(id);
        if (infoResp.getCode() == 0) {
            FundInfoEntity fundInfo = JSON.parseObject(JSON.toJSONString(infoResp.getData()), FundInfoEntity.class);
            fundInfo.setFundType(fundListService.getFundType(id));
            fundInfo.setSaveTime(System.currentTimeMillis());
            mongo.save(fundInfo);
        } else if (infoResp.getCode() == 10001) {
            // 不存在该基金或该接口出错了
            throw JiJuanerException.FUND_INFO_EXCEPTION.putMessage(infoResp.getMsg());
        } else {
            throw JiJuanerException.FEIGN_EXCEPTION;
        }
    }

    @Cacheable(cacheNames = "jijuaner:fundInfo")
    public FundInfoEntity getInfoById(String id) {
        FundInfoEntity findById = mongo.findById(id, FundInfoEntity.class);
        if (findById != null && System.currentTimeMillis() - findById.getSaveTime() < 6 * 60 * 1000) {
            // 6 小时内的数据仍可以使用
            return findById;
        }
        try {
            // 从接口中获取最新数据并保存在数据库中
            saveInfoById(id);
        } catch (JiJuanerException e) {
            // 虽然远程调用失败，但是还是可以先使用旧的数据
            if (JiJuanerException.FEIGN_EXCEPTION.getCode().equals(e.getCode())) {
                return findById;
            } else {
                throw e;
            }
        }
        // 重新从数据库中获取
        return mongo.findById(id, FundInfoEntity.class);
    }

    @Cacheable(cacheNames = "jijuaner:fundSimpleInfo")
    public FundInfoEntity getSimpleInfoById(String id) {
        var query = Query.query(Criteria.where(_ID).is(id));
        query.fields().exclude("acWorthTrend", "ranksInSimilarType", "currentManagers", "scales");
        FundInfoEntity findOne = mongo.findOne(query, FundInfoEntity.class);
        if (findOne == null || System.currentTimeMillis() - findOne.getSaveTime() > 6 * 60 * 1000) {
            saveInfoById(id);
            return mongo.findOne(query, FundInfoEntity.class);
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
