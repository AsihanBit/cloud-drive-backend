package com.netdisk.utils;

import com.netdisk.constant.RedisConstant;
import com.netdisk.constant.TimeConstant;
import com.netdisk.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Data
@AllArgsConstructor
@Slf4j
public class CaptchaUtils {

    private RedisUtil redisUtil;

    // TODO 所有操作 用 operations (Redis 操作管道) 处理保证原子性

    /**
     * 检查令牌生成速率限制
     */
    public boolean checkTokenRateLimit() {
        String clientIp = IpUtils.getClientIp();
        String key = StringUtils.buildKey(RedisConstant.CAPTCHA, RedisConstant.AUTH_TOKEN, clientIp);
        long nowMillis = System.currentTimeMillis();

        // 查询时间窗口内的令牌数量
        Long tokensInWindow = redisUtil.zSetSizeByRange(key, nowMillis - TimeConstant.TOKEN_RATE_LIMIT_MILLIS, nowMillis);

        // 如果为null设为0
        tokensInWindow = tokensInWindow == null ? 0 : tokensInWindow;

        // 检查是否超出限制
        if (tokensInWindow >= TimeConstant.MAX_TOKENS_PER_WINDOW) {
            return false;
        }

        log.info("客户端 {} 在过去 {} 毫秒内已生成 {} 个令牌，限制为 {} 个",
                clientIp, TimeConstant.TOKEN_RATE_LIMIT_MILLIS, tokensInWindow, TimeConstant.MAX_TOKENS_PER_WINDOW);

        return true;
    }


    /**
     * ZSet 生成UUID登录令牌并存储到Redis IP区分
     *
     * @return 生成的UUID令牌
     */
    public String generateLoginTokenZSet() {
        // 生成随机UUID作为令牌
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String clientIp = IpUtils.getClientIp();
        long nowMills = System.currentTimeMillis();

        String key = StringUtils.buildKey(RedisConstant.CAPTCHA, RedisConstant.AUTH_TOKEN, clientIp);
        // 1. 添加至对应ip地址的 ZSet 中
        // 这里毫秒时间戳，现实中不会超出double范围
        redisUtil.zSetAdd(key, uuid, nowMills);
        // 设置过期时间,以及续期
        redisUtil.expire(key, TimeConstant.CAPTCHA_EXPIRE_TIME, TimeUnit.MILLISECONDS);
        // 2. 清除窗口外的令牌
        Long expiredMembers = redisUtil.zSetRemoveRangeByScore(key, 0, nowMills - TimeConstant.SLIDING_WINDOW_SIZE_MILL);
        log.info("移除窗口外的令牌,{}个", expiredMembers);


        // 3. 判断超过5个就清除最早的
        Long zSetSize = redisUtil.getZSetSize(key);
        if (zSetSize != null && zSetSize > 5) {
            // 只保留最新的5个令牌（分数最高的），移除其余的（分数最低的）
            log.info("移除多余的令牌前,令牌数量:{}", redisUtil.getZSetSize(key));
            redisUtil.zSetRemoveRange(key, 0, zSetSize - 6); // 包头包尾
            log.info("移除多余的令牌后,令牌数量:{}", redisUtil.getZSetSize(key));
        }

        return uuid;
    }


    /**
     * 验证登录令牌是否有效
     *
     * @param authToken 要验证的令牌 其实就是uuid
     * @return 如果令牌有效返回true，否则返回false
     */
    public boolean validateLoginTokenZSet(String authToken) {
        if (org.springframework.util.StringUtils.isEmpty(authToken)) {
            return false;
        }

        String clientIp = IpUtils.getClientIp();
        String key = StringUtils.buildKey(RedisConstant.CAPTCHA, RedisConstant.AUTH_TOKEN, clientIp);

        // 获取token的分数，如果存在则返回分数，不存在则返回null
        Double score = redisUtil.zSetScore(key, authToken);

        if (score == null) {
            // 令牌不存在
            return false;
        }

        // 检查令牌是否在有效时间窗口内 多余 因为会自动到期的
//        long tokenTimestamp = score.longValue();
//        long currentTime = System.currentTimeMillis();
//        if (currentTime - tokenTimestamp > TimeConstant.SLIDING_WINDOW_SIZE_MILL) {
//            // 令牌已过期，从ZSet中移除
//            redisUtil.zSetRemove(key, authToken);
//            return false;
//        }

        return true;
    }


    /**
     * 生成UUID登录令牌并存储到Redis: 不会关联指定用户ID,因为还未登录
     *
     * @return 生成的UUID令牌
     */
    public String generateLoginTokenStr() {
        // 生成随机UUID作为令牌
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String key = StringUtils.buildKey(RedisConstant.CAPTCHA, RedisConstant.AUTH_TOKEN);
//        Boolean b = redisUtil.saveStringExpireTime(key, uuid, 60, TimeUnit.SECONDS);
        Long l = redisUtil.addToSetWithExpire(key, uuid, 60, TimeUnit.SECONDS);
        if (l > 0) {
            log.info("生成新的登录授权令牌: 键{}, 值{}", key, uuid);
        }
        return uuid;
    }

    /**
     * 验证登录令牌是否有效
     *
     * @param authToken 前端提交的登录令牌
     * @return 如果令牌有效返回true，否则返回false
     */
    public boolean validateLoginTokenStr(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            log.warn("登录令牌为空");
            return false;
        }
        String key = StringUtils.buildKey(RedisConstant.CAPTCHA, RedisConstant.AUTH_TOKEN);
        // 从Redis获取存储的令牌
        Boolean isExist = redisUtil.isSetMember(key, authToken);

        // 验证令牌是否存在且与提供的令牌匹配
        if (!isExist) {
            log.warn("登录令牌已失效或不存在: {}", authToken);
            throw new BaseException("登录令牌已失效或不存在");
//            return false;
        }

        log.debug("登录令牌验证成功: {}", authToken);
        // 可选：验证成功后删除令牌，防止重复使用
        redisUtil.delete(authToken);

        return true;
    }

}
