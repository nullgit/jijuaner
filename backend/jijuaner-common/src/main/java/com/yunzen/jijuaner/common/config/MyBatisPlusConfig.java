// package com.yunzen.jijuaner.fund.config;

// import com.baomidou.mybatisplus.annotation.DbType;
// import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
// import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

// import org.mybatis.spring.annotation.MapperScan;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.transaction.annotation.EnableTransactionManagement;

// @Configuration
// @EnableTransactionManagement  // 开启事务
// @MapperScan("com.yunzen.jijuaner.fund.dao")
// public class MyBatisPlusConfig {
//     // 引入分页插件
//     @Bean
//     public MybatisPlusInterceptor mybatisPlusInterceptor() {
//         MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
//         interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
//         return interceptor;
//     }
// }
