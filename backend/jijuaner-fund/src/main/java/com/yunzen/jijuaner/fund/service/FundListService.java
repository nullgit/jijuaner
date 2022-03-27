package com.yunzen.jijuaner.fund.service;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.entity.FundListEntity;
import com.yunzen.jijuaner.fund.feign.JSDataFeignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("fundListService")
@Slf4j
public class FundListService {
    @Autowired
    private MongoTemplate mongo;

    @Autowired
    private JSDataFeignService jsDataFeignService;

    /**
     * 更新全部基金列表数据
     */
    public void updateAll() {
        R allListResp = jsDataFeignService.getAllList();
        if (allListResp.getCode() == 0) {
            List<FundListEntity> allList = JSON.parseObject(JSON.toJSONString(allListResp.getData()),
                    new TypeReference<List<FundListEntity>>() {
                    });
            log.info("开始更新基金列表数据，共{}条", allList.size());
            mongo.dropCollection(FundListEntity.class);
            mongo.insert(allList, FundListEntity.class);
            log.info("更新基金列表数据，共{}条", allList.size());
        } else {
            log.error(allListResp.getMsg());
            throw JiJuanerException.FEIGN_EXCEPTION;
        }
    }

    @Cacheable(cacheNames = "jijuaner:fundType")
    public String getFundType(String fundCode) {
        FundListEntity entity = mongo.findById(fundCode, FundListEntity.class);
        if (entity != null) {
            return entity.getFundType();
        } else {
            return null;
        }
    }

    @Cacheable(cacheNames = "jijuaner:fundList")
    public List<FundListEntity> getAll() {
        return mongo.findAll(FundListEntity.class);
    }
}
