package com.netdisk.utils;

import com.netdisk.constant.RedisConstant;
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

    /**
     * 生成UUID登录令牌并存储到Redis: 不会关联指定用户ID,因为还未登录
     *
     * @return 生成的UUID令牌
     */
    public String generateLoginToken() {
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
    public boolean validateLoginToken(String authToken) {
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
