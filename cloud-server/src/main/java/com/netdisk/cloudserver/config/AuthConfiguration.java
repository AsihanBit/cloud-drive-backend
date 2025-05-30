package com.netdisk.cloudserver.config;

import com.netdisk.properties.JwtProperties;
import com.netdisk.utils.BandwidthLimiter;
import com.netdisk.utils.CaptchaUtils;
import com.netdisk.utils.JwtUtil;
import com.netdisk.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jwt令牌 配置类
 */
@Configuration
@Slf4j
public class AuthConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtilUtils(JwtProperties jwtProperties) {
        log.info("开始创建Jwt令牌工具类对象 {}", jwtProperties);
        return new JwtUtil(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), jwtProperties.getUserTokenName());
    }

    @Bean
    @ConditionalOnMissingBean
    public CaptchaUtils captchaUtils(RedisUtil redisUtil) {
        log.info("开始创建验证码工具类对象");
        return new CaptchaUtils(redisUtil);
    }
}
