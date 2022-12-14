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
     * @return ??????????????????
     * @throws AlipayApiException
     */
    public String subscribe(Integer userId, String fundCode, String token, String amountStr) throws AlipayApiException {
        // TODO ??????(???????????????3:00???)????????????????????????????????????
        if (!StringUtils.hasText(token) || userId == null || !StringUtils.hasText(fundCode)
                || !StringUtils.hasText(amountStr)) {
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("???????????????, ????????????????????????????????????");
        }
        BigInteger amount = new BigDecimal(amountStr).multiply(BigDecimal.valueOf(100L)).toBigInteger();
        FundPayInfoEntity fundPayInfo = fundPayInfoService.getPayFundInfo(fundCode);
        if ("????????????".equals(fundPayInfo.getSubscriptionStatus())) {
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("?????????????????????");
        } else if (amount.compareTo(fundPayInfo.getMinAmount()) < 0) {
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("??????????????????????????????");
        } else if (amount.compareTo(fundPayInfo.getMaxAmountPerDay()) > 0) {
            // TODO ??????????????????????????????
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("?????????????????????????????????????????????");
        }

        // ?????? lua ???????????????????????????????????????
        Long result = redis.execute(new DefaultRedisScript<Long>("if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then return redis.call('del', KEYS[1]) else return 0 end", Long.class),
                Arrays.asList(JiJuanerConstantString.PAY_TOKEN.getConstant() + userId), // KEYS
                token // ARGV
        );
        if (result == 0L) {
            // ??????????????????
            throw JiJuanerException.SUBSCRIBE_EXCEPTION.putMessage("???????????????, ?????????????????????");
        }

        var entity = new TransactionEntity();
        entity.setUserId(userId);
        entity.setFundCode(fundCode);
        // ????????????????????????: PayUtils.countService(amount, fundPayInfoService.getServiceCharge(fundCode))
        entity.setAmount(amount);
        entity.setTime(System.currentTimeMillis());
        entity.setType(TransactionType.UNPAYED_SUBSCRIBE);
        baseMapper.insert(entity);

        var payVo = new AliPayVo();
        payVo.setOutTradeNo(entity.getId().toString());
        payVo.setTotalAmount(new BigDecimal(amount, 2).toString());
        payVo.setSubject("????????????, ??????" + fundCode);
        payVo.setBody("?????????");
        String payPage = alipay.pay(payVo);

        // ????????????????????????????????????, ??????????????????
        rabbit.convertAndSend(TransactionRabbitConfig.TRANSACTION_EXCHANGE, TransactionRabbitConfig.SUBSCRIBE_CREATE,
                entity.getId(),
                new CorrelationData(entity.getId() + UUID.randomUUID().toString()) // ?????????id, ???????????????????????????????????????
        );
        return payPage;
    }

    /**
     * ???????????????????????????, ??????????????????????????????????????????
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
        // ???????????????????????????
        if (entity.getType() != TransactionType.UNPAYED_SUBSCRIBE) {
            log.warn("???????????????");
        } else if (order.getTradeStatus() == TradeStatus.TRADE_SUCCESS) {
            // ??????????????????????????????
            entity.setType(TransactionType.PAYED_SUBSCRIBE);
        } else if (order.getTradeStatus() == TradeStatus.TRADE_CLOSED) {
            // ??????????????????, ???????????????????????????
            entity.setType(TransactionType.SUBSCRIBE_TIMEOUT);
        } else if (order.getTradeStatus() == TradeStatus.TRADE_FINISHED) {
            // ??????, ????????????, ??????????????????????????????, ???????????????????????????
            entity.setType(TransactionType.SUBSCRIBE_DONE);
        }
        // ?????????????????????/?????????????????????
        entity.setTime(System.currentTimeMillis());
        baseMapper.updateById(entity);
    }

    /**
     * ??????????????????
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @RabbitListener(queues = { TransactionRabbitConfig.TRANSACTION_CLOSE_QUEUE })
    private void closeTransaction(Long id, Channel channel, Message message) throws IOException, InterruptedException {
        try {
            // TODO ?????????????????????????????????????????????
            TransactionEntity entity = getById(id, ID, TYPE);
            if (entity.getType() == TransactionType.UNPAYED_SUBSCRIBE) {
                entity.setType(TransactionType.SUBSCRIBE_TIMEOUT);
                updateById(entity);
            }
            // ???????????????
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            Thread.sleep(1000);
            log.error("??????????????????", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    public void redeem() {
    }
}
