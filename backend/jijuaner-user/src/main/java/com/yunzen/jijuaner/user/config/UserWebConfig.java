package com.yunzen.jijuaner.user.config;

import com.yunzen.jijuaner.common.interceptor.UserInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UserWebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserInterceptor())// 注册拦截器
                .addPathPatterns("/**");
    }

    // @Bean
    // public CorsFilter corsFilter() {
    //     final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     final CorsConfiguration corsConfiguration = new CorsConfiguration();

    //     // 允许的请求头
    //     corsConfiguration.addAllowedHeader("*");
    //     // 允许跨域请求的域名
    //     corsConfiguration.addAllowedOriginPattern("*");
    //     // 设置允许的方法
    //     corsConfiguration.addAllowedMethod("*");
    //     // 是否允许证书（cookie）
    //     corsConfiguration.setAllowCredentials(true);
    //     // 跨域允许时间
    //     corsConfiguration.setMaxAge(3600L);
    //     source.registerCorsConfiguration("/**", corsConfiguration);
    //     return new CorsFilter(source);
    // }

}
