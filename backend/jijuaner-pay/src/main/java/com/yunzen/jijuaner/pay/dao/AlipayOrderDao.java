package com.yunzen.jijuaner.pay.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yunzen.jijuaner.pay.entity.AlipayOrderEntity;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlipayOrderDao extends BaseMapper<AlipayOrderEntity> {
}
