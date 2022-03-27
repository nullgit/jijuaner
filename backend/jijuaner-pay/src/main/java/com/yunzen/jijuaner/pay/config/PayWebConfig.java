package com.yunzen.jijuaner.pay.config;

import com.yunzen.jijuaner.common.interceptor.UserInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PayWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor())  // 注册拦截器
                .addPathPatterns("/**");
    }
}
