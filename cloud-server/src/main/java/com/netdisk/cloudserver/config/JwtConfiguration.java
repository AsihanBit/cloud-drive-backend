package com.netdisk.cloudserver.config;

import com.netdisk.properties.DiskProperties;
import com.netdisk.properties.JwtProperties;
import com.netdisk.utils.FileChunkUtil;
import com.netdisk.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jwt令牌 配置类
 */
@Configuration
@Slf4j
public class JwtConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtilUtil(JwtProperties jwtProperties) {
        log.info("开始创建Jwt令牌工具类对象 {}", jwtProperties);
        return new JwtUtil(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), jwtProperties.getUserTokenName());
    }
}
