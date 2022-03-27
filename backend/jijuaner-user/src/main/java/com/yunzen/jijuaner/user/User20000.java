package com.yunzen.jijuaner.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60*60*24*30)
@EnableCaching
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class User20000 {
    public static void main(String[] args) {
        SpringApplication.run(User20000.class, args);
    }
}
