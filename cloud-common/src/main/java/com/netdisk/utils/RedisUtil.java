package com.netdisk.utils;

import com.netdisk.constant.RedisConstant;
import com.netdisk.constant.TimeConstant;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@NoArgsConstructor
//@Component // 或者这里加@Component 或者 config里@Bean
public class RedisUtil {

    private RedisTemplate<String, Object> redisTemplate;

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
     * 执行Redis事务
     *
     * @param callback 事务回调
     * @return 执行结果
     */
    public <T> T executeTransaction(SessionCallback<T> callback) {
        return redisTemplate.execute(callback);
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
     * 向有序集合添加元素
     *
     * @param key   键
     * @param value 值
     * @param score 分数
     * @return 添加成功返回true
     */
    public Boolean zSetAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取有序集合的大小
     *
     * @param key 键
     * @return 集合大小
     */
    public Long getZSetSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    public Long zSetSizeByRange(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取ZSet中指定成员的分数
     *
     * @param key    Redis键
     * @param member 成员
     * @return 成员的分数，如果成员不存在则返回null
     */
    public Double zSetScore(String key, Object member) {
        return redisTemplate.opsForZSet().score(key, member);
    }

    /**
     * 从ZSet中移除指定成员
     *
     * @param key     Redis键
     * @param members 要移除的成员
     * @return 成功移除的成员数量
     */
    public Long zSetRemove(String key, Object... members) {
        return redisTemplate.opsForZSet().remove(key, members);
    }

    /**
     * 移除ZSet中指定排名范围的成员
     *
     * @param key   Redis键
     * @param start 开始排名（最小分数为0）
     * @param end   结束排名（最大分数为-1）
     * @return 移除的成员数量
     */
    public Long zSetRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 移除有序集合中指定分数范围的元素
     *
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 移除的元素数量
     */
    public Long zSetRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     * 获取有序集合中指定范围的元素（带分数）
     *
     * @param key   键
     * @param start 开始索引
     * @param end   结束索引
     * @return 元素集合
     */
    public Set<ZSetOperations.TypedTuple<Object>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
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
