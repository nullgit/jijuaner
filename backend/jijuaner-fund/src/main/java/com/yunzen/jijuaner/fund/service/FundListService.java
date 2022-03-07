package com.yunzen.jijuaner.fund.service;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunzen.jijuaner.common.exception.FeignException;
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
    private MongoTemplate mongoTemplate;

    @Autowired
    private JSDataFeignService jsDataFeignService;

    public void updateAll() {
        R allListResp = jsDataFeignService.getAllList();
        if (allListResp.getCode() == 0) {
            List<FundListEntity> allList = JSON.parseObject(JSON.toJSONString(allListResp.getData()),
                    new TypeReference<List<FundListEntity>>() {
                    });
            log.info("更新基金列表数据，共{}条", allList.size());
            mongoTemplate.insert(allList, FundListEntity.class);
        } else {
            throw new FeignException();
        }
    }

    @Cacheable(cacheNames = "jijuaner:fundType")
    public String getFundType(String fundCode) {
        FundListEntity entity = mongoTemplate.findById(fundCode, FundListEntity.class);
        if (entity != null) {
            return entity.getFundType();
        } else {
            return null;
        }
    }

    @Cacheable(cacheNames = "jijuaner:fundList")
    public List<FundListEntity> getAll() {
        return mongoTemplate.findAll(FundListEntity.class);
    }
}
