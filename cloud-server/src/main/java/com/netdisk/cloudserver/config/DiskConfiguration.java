package com.netdisk.cloudserver.config;


import com.netdisk.properties.DiskProperties;

import com.netdisk.utils.FileChunkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储路径 配置类
 */
@Configuration
@Slf4j
public class DiskConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileChunkUtil fileMergeUtil(DiskProperties diskProperties) {
        log.info("开始创建分片上传工具类对象 {}", diskProperties);
        return new FileChunkUtil(diskProperties.getStoragePath(), diskProperties.getFileDir(), diskProperties.getTempDir());
    }
}
