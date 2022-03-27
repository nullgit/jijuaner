package com.yunzen.jijuaner.user.config;

import com.yunzen.jijuaner.common.interceptor.UserInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UserWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor()) // 注册 User 拦截器
                .addPathPatterns("/**");
    }
}
