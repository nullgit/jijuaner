package com.yunzen.jijuaner.pay.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
public class JiJuanerSessionConfig {
    @Value("${config.domain}")  // 配置文件中的属性
    public String domain;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        // 放大作用域
        cookieSerializer.setDomainName(domain);
        cookieSerializer.setCookieName("JIJUANERSESSION");
        cookieSerializer.setCookieMaxAge(60 * 60 * 24 * 30);
        return cookieSerializer;
    }

    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
}
