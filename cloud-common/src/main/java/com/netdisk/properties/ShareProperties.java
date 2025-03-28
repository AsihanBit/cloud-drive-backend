package com.netdisk.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud-drive.share")
@Data
public class ShareProperties {
    private ShareLink link;

    @Data
    @AllArgsConstructor
    public static class ShareLink {
        private String protocol;
        private String host;
        private Integer port;
        private String path;

        // 生成完整的 分享链接
        public String getShareLink() {
            return protocol + "://" + host + ":" + port + path;
        }
    }
}
