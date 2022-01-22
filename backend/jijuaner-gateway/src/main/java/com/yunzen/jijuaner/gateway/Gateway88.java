package com.yunzen.jijuaner.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Wang Yunze
 * @create 2021-12-29 9:42
 */
@EnableDiscoveryClient
@SpringBootApplication
public class Gateway88 {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Gateway88.class, args);
    }
}
