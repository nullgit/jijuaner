package com.yunzen.jijuaner.pay.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunzen.jijuaner.pay.dao.PropertyDao;
import com.yunzen.jijuaner.pay.entity.PropertyEntity;

import org.springframework.stereotype.Service;

@Service("propertyService")
public class PropertyService extends ServiceImpl<PropertyDao, PropertyEntity> {
}
