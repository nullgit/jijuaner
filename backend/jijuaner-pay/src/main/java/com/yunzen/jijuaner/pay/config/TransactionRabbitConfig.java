package com.yunzen.jijuaner.pay.config;

import java.util.HashMap;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableRabbit
@Slf4j
public class TransactionRabbitConfig {
    public static final String TRANSACTION_EXCHANGE = "transaction-exchange";
    public static final String TRANSACTION_DELAY_QUEUE = "transaction-delay-queue";
    public static final String TRANSACTION_CLOSE_QUEUE = "transaction-close-queue";
    public static final String SUBSCRIBE_CLOSE = "subscribe.close";
    public static final String SUBSCRIBE_CREATE = "subscribe.create";

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var rabbit = new RabbitTemplate(connectionFactory);
        rabbit.setMessageConverter(messageConverter());

        /**
         * 发送方确认, 消息投递成功或失败都触发该回调
         * correlationData: 当前消息的唯一关联数据(这个是消息的唯一id)
         * ack: 消息是否成功收到, 只要消息抵达Broker后为true
         * cause: 失败的原因
         */
        rabbit.setConfirmCallback((correlationData, ack, cause) -> {
            // TODO 处理消息没投递成功
            log.error("correlationData:" + correlationData + "ack:" + ack + "cause" + cause);
        });
        /**
         * 发送方确认, 消息没有投递给指定的队列, 就触发这个失败回调
         * message: 投递失败的消息详细信息
         * replyCode: 回复的状态码
         * replyText: 回复的文本内容
         * exchange: 当时这个消息发给哪个交换机
         * routingKey: 当时这个消息用哪个路邮键
         */
        rabbit.setReturnsCallback((returnMessage) -> {
            log.error("msg:" + returnMessage.getMessage() + "code:" + returnMessage.getReplyCode() + "text"
                    + returnMessage.getReplyText() + "exchange:" + returnMessage.getExchange() + "routekey"
                    + returnMessage.getRoutingKey());
        });
        return rabbit;
    }

    // 使用json保存消息
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 交易服务交换机/也是死信交换机
    @Bean
    public Exchange transactionEventExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE, true, false);
    }

    // 延迟队列
    @Bean
    public Queue transactionDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", TRANSACTION_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", SUBSCRIBE_CLOSE);
        // 消息过期时间 6分钟
        arguments.put("x-message-ttl", 60 * 1000 * 6);
        return new Queue(TRANSACTION_DELAY_QUEUE, true, false, false, arguments);
    }

    // 死信队列
    @Bean
    public Queue subscribeCloseQueue() {
        return new Queue(TRANSACTION_CLOSE_QUEUE, true, false, false);
    }

    // 死信交换机与死信队列绑定
    @Bean
    public Binding subscribeCloseBinding() {
        return new Binding(TRANSACTION_CLOSE_QUEUE, Binding.DestinationType.QUEUE, TRANSACTION_EXCHANGE,
                SUBSCRIBE_CLOSE, null);
    }

    // 交易服务交换机与延迟队列绑定
    @Bean
    public Binding subscribeCreateBinding() {
        return new Binding(TRANSACTION_DELAY_QUEUE, Binding.DestinationType.QUEUE, TRANSACTION_EXCHANGE,
                SUBSCRIBE_CREATE, null);
    }
}
