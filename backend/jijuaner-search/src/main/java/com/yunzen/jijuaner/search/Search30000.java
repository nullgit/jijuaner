package com.yunzen.jijuaner.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

// @EnableCaching
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class Search30000 {
    public static void main(String[] args) {
        SpringApplication.run(Search30000.class, args);
    }
}
