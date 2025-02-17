package com.netdisk.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud-drive.jwt")
@Data
public class JwtProperties {
    /*
    *
    *     user-secret-key: cloud
    # 7200000 2小时
    user-ttl: 7200000
    user-token-name: authentication*/

    /**
     * 用户端 jwt令牌相关配置
     */
    private String userSecretKey;
    private Long userTtl;
    private String userTokenName;
}
