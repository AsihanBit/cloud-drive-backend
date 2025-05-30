package com.netdisk.utils;

import com.netdisk.constant.RedisConstant;
import com.netdisk.constant.TimeConstant;
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

    /**
     * 构造方法注入 RedisTemplate
     *
     * @param redisTemplate Redis模板
     */
    public RedisUtil(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * ======================= 只专注Redis自身 =======================
     */
    /**
     * 保存字符串
     *
     * @param key
     * @param value
     * @param expireTime
     * @param timeUnit
     * @return
     */
    public Boolean saveStringExpireTime(String key, String value, long expireTime, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
        return true;
    }

    /**
     * 验证并获取字符串
     *
     * @param key
     * @return
     */
    public String getString(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置键的过期时间
     *
     * @param key     键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 设置是否成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 删除指定的 key
     *
     * @param key 键
     * @return 成功返回 true, 失败返回 false
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }


    /**
     * ======================= 集合相关 =======================
     */

    /**
     * 往 Set 集合中添加元素
     *
     * @param key   键
     * @param value 值
     * @return 添加成功的数量
     */
    public Long addToSet(String key, Object value) {
        return redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 往 Set 集合中添加元素，并设置过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 添加成功的数量，如果添加失败或设置过期时间失败则返回0
     */
    public Long addToSetWithExpire(String key, Object value, long timeout, TimeUnit unit) {
        Long count = redisTemplate.opsForSet().add(key, value);

        // 如果添加成功且数量大于0，设置过期时间
        if (count != null && count > 0) {
            Boolean setExpireResult = redisTemplate.expire(key, timeout, unit);

            // 如果设置过期时间失败，记录日志
            if (Boolean.FALSE.equals(setExpireResult)) {
                log.warn("无法为集合设置过期时间: {}", key);
                // 可选：如果设置过期时间失败，可以考虑删除刚刚添加的元素
                redisTemplate.opsForSet().remove(key, value);
                return 0L;
            }
        }

        return count;
    }

    /**
     * 判断元素是否是集合的成员
     *
     * @param key   键
     * @param value 值
     * @return 是否为成员
     */
    public Boolean isSetMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取集合的大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }


    /**
     * ======================= 有序集合相关 =======================
     */

    /**
     * 获取有序集合的大小
     *
     * @param key 键
     * @return 有序集合的大小
     */
    public Long getZSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 添加ZSET中的元素，使用时间戳作为分数 最好是毫秒防止网络波动
     *
     * @param key   键
     * @param value 值
     * @param score 排序分数
     * @return 是否添加成功
     */
    public Boolean addZSet(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * ZSet 范围移除
     *
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long removeZSetRange(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }


    // ======================= 文件分片 =======================

    /**
     * 保存分片信息至 redis
     */
    public void recordChunkUpload(Integer userId, String fileMd5, Integer chunkNumber) {
        // 构造 Redis 集合的 key
        String key = "file_chunk:" + userId + ":" + fileMd5;
//        String key = StringUtils.buildKey(RedisConstant.FILE_CHUNK, userId.toString(), fileMd5);
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
