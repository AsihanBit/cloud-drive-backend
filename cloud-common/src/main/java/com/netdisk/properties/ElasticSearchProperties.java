package com.netdisk.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elastic-search")
@Data
public class ElasticSearchProperties {
    private String host;
    private Integer port;
    private String scheme;
    // 用户名
    private String username;
    // 密码
    private String password;
}
