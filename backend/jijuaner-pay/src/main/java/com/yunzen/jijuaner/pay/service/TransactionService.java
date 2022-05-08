package com.yunzen.jijuaner.pay.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.UUID;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rabbitmq.client.Channel;
import com.yunzen.jijuaner.common.exception.JiJuanerException;
import com.yunzen.jijuaner.common.utils.JiJuanerConstantString;
import com.yunzen.jijuaner.pay.config.AlipayTemplate;
import com.yunzen.jijuaner.pay.config.TransactionRabbitConfig;
import com.yunzen.jijuaner.pay.dao.TransactionDao;
import com.yunzen.jijuaner.pay.entity.AlipayOrderEntity;
import com.yunzen.jijuaner.pay.entity.AlipayOrderEntity.TradeStatus;
import com.yunzen.jijuaner.pay.entity.FundPayInfoEntity;
import com.yunzen.jijuaner.pay.entity.TransactionEntity;
import com.yunzen.jijuaner.pay.entity.TransactionEntity.TransactionType;
import com.yunzen.jijuaner.pay.vo.AliPayVo;
import com.yunzen.jijuaner.pay.vo.AlipayAsyncVo;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Service("transactionService")
@Slf4j
public class TransactionService extends ServiceImpl<TransactionDao, TransactionEntity> {
    private static final String ID = "id";
    private static final String USER_ID = "user_id";
    private static final String AMOUNT = "amount";
    private static final String TYPE = "type";
    private static final String FUND_CODE = "fund_code";

    @Autowired
    private AlipayOrderService alipayOrderService;

    @Autowired
    private FundPayInfoService fundPayInfoService;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private AlipayTemplate alipay;

    @Autowired
    private RabbitTemplate rabbit;

    public TransactionEntity getByIdAndUserId(Long id, Integer userId, String... columns) {
        return baseMapper.selectOne(
                new QueryWrapper<TransactionEntity>().select(columns).eq(ID, id).and(qw -> qw.eq(USER_ID, userId)));
    }

    public TransactionEntity getById(Long id, String... columns) {
        return baseMapper.selectOne(
                new QueryWrapper<TransactionEntity>().select(columns).eq(ID, id));
    }

    /**
     * @return 返回支付网页
     * @throws AlipayApiException
     */
    public String subscribe(Integer userId, String fundCode, String token, String amountStr) throws AlipayApiException {
        // TODO 定时(交易日下午3:00后)将申购记录发送给基金公司
        if (!StringUtils.hasText(token) || userId == null || !StringUtils.hasText(fundCode)
                || !StringUtils.hasText(amountStr)) {
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("验证不通过, 请重新填写表单或刷新页面");
        }
        BigInteger amount = new BigDecimal(amountStr).multiply(BigDecimal.valueOf(100L)).toBigInteger();
        FundPayInfoEntity fundPayInfo = fundPayInfoService.getPayFundInfo(fundCode);
        if ("暂停申购".equals(fundPayInfo.getSubscriptionStatus())) {
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("该基金暂停申购");
        } else if (amount.compareTo(fundPayInfo.getMinAmount()) < 0) {
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("单笔申购小于最低金额");
        } else if (amount.compareTo(fundPayInfo.getMaxAmountPerDay()) > 0) {
            // TODO 统计今日已申购的金额
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("单笔申购或今日申购金额最大限额");
        }

        // 通过 lua 脚本原子验证令牌和删除令牌
        Long result = redis.execute(new DefaultRedisScript<Long>("if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) else return 0 end", Long.class),
                Arrays.asList(JiJuanerConstantString.PAY_TOKEN.getConstant() + userId), // KEYS
                token // ARGV
        );
        if (result == 0L) {
            // 令牌验证失败
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("验证不通过, 请重新刷新页面");
        }

        var entity = new TransactionEntity();
        entity.setUserId(userId);
        entity.setFundCode(fundCode);
        // 扣除手续费后金额: PayUtils.countService(amount, fundPayInfoService.getServiceCharge(fundCode))
        entity.setAmount(amount);
        entity.setTime(System.currentTimeMillis());
        entity.setType(TransactionType.UNPAYED_SUBSCRIBE);
        baseMapper.insert(entity);

        var payVo = new AliPayVo();
        payVo.setOutTradeNo(entity.getId().toString());
        payVo.setTotalAmount(new BigDecimal(amount, 2).toString());
        payVo.setSubject("购买基金, 代码" + fundCode);
        payVo.setBody("无详情");
        String payPage = alipay.pay(payVo);

        // 向消息队列发送开单的消息, 设置定时关单
        rabbit.convertAndSend(TransactionRabbitConfig.TRANSACTION_EXCHANGE, TransactionRabbitConfig.SUBSCRIBE_CREATE,
                entity.getId(),
                new CorrelationData(entity.getId() + UUID.randomUUID().toString()) // 消息的id, 用让发送端判断消息是否到达
        );
        return payPage;
    }

    /**
     * 支付回调通知成功后, 将交易状态从未支付改为已支付
     */
    public void handlePayResult(AlipayAsyncVo vo) {
        Long id = Long.parseLong(vo.getOut_trade_no());
        var order = new AlipayOrderEntity();
        order.setId(id);
        order.setTradeNo(vo.getTrade_no());
        order.setBuyerId(vo.getBuyer_id());
        order.setSellerId(vo.getSeller_id());
        order.setBuyerPayAmount(new BigDecimal(vo.getBuyer_pay_amount()));
        order.setTotalAmount(new BigDecimal(vo.getTotal_amount()));
        order.setReceiptAmount(new BigDecimal(vo.getReceipt_amount()));
        order.setInvoiceAmount(new BigDecimal(vo.getInvoice_amount()));
        order.setSubject(vo.getSubject());
        order.setBody(vo.getBody());
        order.setGmtCreate(vo.getGmt_create());
        order.setGmtPayment(vo.getGmt_payment());
        order.setGmtClose(vo.getGmt_close());
        order.setNotifyTime(vo.getNotify_time());
        order.setNotifyId(vo.getNotify_id());
        order.setNotifyType(vo.getNotify_type());
        order.setFundBillList(vo.getFund_bill_list());
        order.setTradeStatus(TradeStatus.valueOf(vo.getTrade_status()));
        alipayOrderService.save(order);

        TransactionEntity entity = baseMapper
                .selectOne(new QueryWrapper<TransactionEntity>().eq(ID, id).select(ID, TYPE));
        // 检查原来的交易状态
        if (entity.getType() != TransactionType.UNPAYED_SUBSCRIBE) {
            log.warn("订单已支付");
        } else if (order.getTradeStatus() == TradeStatus.TRADE_SUCCESS) {
            // 将交易状态改为已支付
            entity.setType(TransactionType.PAYED_SUBSCRIBE);
        } else if (order.getTradeStatus() == TradeStatus.TRADE_CLOSED) {
            // 交易超时关闭, 电脑网站支付不支持
            entity.setType(TransactionType.SUBSCRIBE_TIMEOUT);
        } else if (order.getTradeStatus() == TradeStatus.TRADE_FINISHED) {
            // 关单, 交易终止, 之后无法执行退款流程, 电脑网站支付不支持
            entity.setType(TransactionType.SUBSCRIBE_DONE);
        }
        // 修改为成功支付/交易关闭的时间
        entity.setTime(System.currentTimeMillis());
        baseMapper.updateById(entity);
    }

    /**
     * 处理关单消息
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @RabbitListener(queues = { TransactionRabbitConfig.TRANSACTION_CLOSE_QUEUE })
    private void closeTransaction(Long id, Channel channel, Message message) throws IOException, InterruptedException {
        try {
            // TODO 可以在此处载手动调用支付宝收单
            TransactionEntity entity = getById(id, ID, TYPE);
            if (entity.getType() == TransactionType.UNPAYED_SUBSCRIBE) {
                entity.setType(TransactionType.SUBSCRIBE_TIMEOUT);
                updateById(entity);
            }
            // 接受方确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            Thread.sleep(1000);
            log.error("消息消费错误", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    public void redeem() {
    }
}
