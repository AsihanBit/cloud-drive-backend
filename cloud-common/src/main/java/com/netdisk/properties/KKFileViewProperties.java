package com.netdisk.properties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "kkfileview")
@Data
public class KKFileViewProperties {
    private Origin origin;
    private Preview preview;


    @Data
    @AllArgsConstructor
    public static class Origin {
        private String protocol;
        private String host;
        private Integer port;
        private String path;

        // 生成完整的 originUrl
        public String getOriginUrl() {
            return protocol + "://" + host + ":" + port + path;
        }
    }

    @Data
    @AllArgsConstructor
    public static class Preview {
        private String protocol;
        private String host;
        private Integer port;
        private String path;

        // 生成完整的 previewUrl
        public String getPreviewUrl() {
            return protocol + "://" + host + ":" + port + path;
        }
    }
}
