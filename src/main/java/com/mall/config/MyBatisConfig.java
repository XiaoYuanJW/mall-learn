package com.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 */
@Configuration
@MapperScan({"com.mall.mbg.mapper","com.mall.dao"})
public class MyBatisConfig {
}
