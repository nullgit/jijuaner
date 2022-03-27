package com.yunzen.jijuaner.fund.config;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 线程池配置
 */
@Configuration
public class ThreadConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadProperties threadProperties) {
        return new ThreadPoolExecutor(threadProperties.getCoreSize(), threadProperties.getMaxSize(),
                threadProperties.getKeepAliveTime(), TimeUnit.SECONDS, new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }
}

@ConfigurationProperties(prefix = "config.thread")
@Data
@Component
class ThreadProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
