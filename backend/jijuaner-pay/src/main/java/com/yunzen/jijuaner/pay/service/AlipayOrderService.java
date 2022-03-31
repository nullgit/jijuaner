package com.yunzen.jijuaner.pay.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunzen.jijuaner.pay.dao.AlipayOrderDao;
import com.yunzen.jijuaner.pay.entity.AlipayOrderEntity;

import org.springframework.stereotype.Service;

@Service("alipayOrderService")
public class AlipayOrderService extends ServiceImpl<AlipayOrderDao, AlipayOrderEntity> {
}
