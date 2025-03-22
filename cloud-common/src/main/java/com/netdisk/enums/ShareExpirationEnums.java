package com.netdisk.enums;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public enum ShareExpirationEnums {
    ONE_DAY(0, 1, "1天"),
    ONE_WEEK(1, 7, "7天"),
    ONE_MONTH(2, 30, "30天"),
    PERMANENT(3, -1, "永久有效"); // -1 表示永久有效

    private final Integer code; // 枚举值对应的代码
    private final Integer days; // 对应的天数
    private final String desc;

    /**
     * 构造函数
     *
     * @param code 枚举值对应的代码
     * @param days 对应的天数
     */
    ShareExpirationEnums(int code, int days, String desc) {
        this.code = code;
        this.days = days;
        this.desc = desc;
    }

    /**
     * 根据代码获取对应的枚举值
     *
     * @param code 代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码无效
     */
    public static ShareExpirationEnums getExpirationByCode(int code) {
        for (ShareExpirationEnums expiration : ShareExpirationEnums.values()) {
            if (expiration.getCode() == code) {
                return expiration;
            }
        }
        throw new IllegalArgumentException("Invalid ShareExpiration code: " + code);
    }

    /**
     * 计算到期时间
     *
     * @param createTime 创建时间
     * @return 到期时间（如果永久有效，返回 null）
     */
    public LocalDateTime calculateExpireTime(LocalDateTime createTime) {
        if (this.days == -1) {
            return null; // 永久有效
        }
        return createTime.plusDays(this.days);
    }


}
