package com.netdisk.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cloud-drive.disk")
@Data
public class DiskProperties {
    // 存储路径
    private String storagePath;
    private String tempDir;
}
