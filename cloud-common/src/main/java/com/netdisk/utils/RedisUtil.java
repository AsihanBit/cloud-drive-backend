package com.netdisk.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@NoArgsConstructor
//@Component // 或者这里加@Component 或者 config里@Bean
public class RedisUtil {

    private RedisTemplate redisTemplate;

    public RedisUtil(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ======================= 文件分片 =======================

    /**
     * 保存分片信息至 redis
     */
    public void recordChunkUpload(Integer userId, String fileMd5, Integer chunkNumber) {
        // 构造 Redis 集合的 key
        String key = "file_chunk:" + userId + ":" + fileMd5;
        // 将分片位置下标添加到集合中
        redisTemplate.opsForSet().add(key, chunkNumber);
        log.info("已记录分片信息: 用户ID {}, 文件MD5 {}, 分片位置 {}", userId, fileMd5, chunkNumber);
    }

    /**
     * 检查用户文件的某个位置下标分片是否存在
     */
    public boolean checkChunkExists(Integer userId, String fileMd5, Integer chunkNumber) {
        String key = "file_chunk:" + userId + ":" + fileMd5;
        return redisTemplate.opsForSet().isMember(key, chunkNumber);
    }


    /**
     * 获取文件的所有分片位置的数量
     */
    public Long getChunkCount(Integer userId, String fileMd5) {
        String key = "file_chunk:" + userId + ":" + fileMd5;
        return redisTemplate.opsForSet().size(key);
    }


    /**
     * 删除文件的所有分片信息
     */
    public void deleteAllChunk(Integer userId, String fileMd5) {
        String key = "file_chunk:" + userId + ":" + fileMd5;
        redisTemplate.delete(key);
        log.info("已删除文件的所有分片信息: 用户ID {}, 文件MD5 {}", userId, fileMd5);
    }

    // ======================= Redis 锁相关 =======================

    /**
     * 尝试加锁
     *
     * @param lockKey  锁的 key
     * @param value    锁的值，通常可以是任意标识
     * @param timeout  锁的超时时间
     * @param timeUnit 超时时间单位
     * @return 是否成功获取锁
     */
    public boolean tryLock(String lockKey, String value, long timeout, TimeUnit timeUnit) {
        Boolean lockAcquired = redisTemplate.opsForValue().setIfAbsent(lockKey, value, timeout, timeUnit);
        return Boolean.TRUE.equals(lockAcquired);
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的 key
     */
    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
        log.info("已释放锁: {}", lockKey);
    }

    // ======================= 文件防止递归 =======================
    public void recordSavedItemId(Integer userId, Integer shareId, Integer savedItemId) {
        String key = "savedItemIds:" + userId + ":" + shareId;
        redisTemplate.opsForSet().add(key, savedItemId);
    }

    public boolean checkSavedItemIdExists(Integer userId, Integer shareId, Integer savedItemId) {
        String key = "savedItemIds:" + userId + ":" + shareId;
        return redisTemplate.opsForSet().isMember(key, savedItemId);
    }

    public void deleteAllSavedItemIds(Integer userId, Integer shareId) {
        String key = "savedItemIds:" + userId + ":" + shareId;
        redisTemplate.delete(key);
    }


}
