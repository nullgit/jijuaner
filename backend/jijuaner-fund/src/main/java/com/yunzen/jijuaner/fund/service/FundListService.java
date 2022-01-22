package com.yunzen.jijuaner.fund.service;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunzen.jijuaner.common.exception.FeignException;
import com.yunzen.jijuaner.common.utils.R;
import com.yunzen.jijuaner.fund.dao.FundListDao;
import com.yunzen.jijuaner.fund.entity.FundListEntity;
import com.yunzen.jijuaner.fund.feign.JSDataFeignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service("fundListService")
@Slf4j
public class FundListService extends ServiceImpl<FundListDao, FundListEntity> {
    @Autowired
    private JSDataFeignService jsDataFeignService;

    public void updateAll() {
        R allListResp = jsDataFeignService.getAllList();
        if (allListResp.getCode() == 0) {
            List<FundListEntity> allList = JSON.parseObject((String) allListResp.getData(),
                    new TypeReference<List<FundListEntity>>() {
                    });
            log.info("更新基金列表数据，共{}条", allList.size());
            this.saveOrUpdateBatch(allList);
        } else {
            throw new FeignException();
        }
    }

    @Cacheable(cacheNames = "jijuaner:fundName")
    public String getFundName(String fundCode) {
        if (fundCode.length() > 6) {
            return "不存在的基金代码";
        }
        FundListEntity entity = baseMapper.selectById(fundCode);
        if (entity != null) {
            return entity.getFundName();
        } else {
            return "不存在的基金代码";
        }
    }
}
