package com.netdisk.cloudserver.config;


import com.netdisk.properties.DiskProperties;

import com.netdisk.utils.BandwidthLimiter;
import com.netdisk.utils.FileChunkUtil;
import com.netdisk.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * IO 配置类: 网络 磁盘
 */
@Configuration
@Slf4j
public class IOConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FileChunkUtil fileMergeUtil(DiskProperties diskProperties, RedisUtil redisUtil) {
        log.info("开始创建分片上传工具类对象 {}", diskProperties);
        return new FileChunkUtil(diskProperties.getStoragePath(), diskProperties.getFileDir(), diskProperties.getTempDir(), redisUtil);
    }

    @Bean
    @ConditionalOnMissingBean
    public BandwidthLimiter bandwidthLimiter(RedisUtil redisUtil) {
        return new BandwidthLimiter(redisUtil);
    }
}
