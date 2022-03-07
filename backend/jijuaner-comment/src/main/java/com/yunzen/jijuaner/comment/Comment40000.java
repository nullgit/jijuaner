package com.yunzen.jijuaner.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Wang Yunze
 * @create 2021-12-29 9:42
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class Comment40000 {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Comment40000.class, args);
    }
}
