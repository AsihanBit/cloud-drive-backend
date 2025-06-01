package com.netdisk.constant;

/**
 * 时间常量类
 */
public class TimeConstant {
    /**
     * 一秒中的纳秒数
     */
    public static final long NANOS_PER_SECOND = 1_000_000_000L;

    /**
     * 一秒中的毫秒数
     */
    public static final long MILLIS_PER_SECOND = 1_000L;

    /**
     * 一毫秒中的纳秒数
     */
    public static final long NANOS_PER_MILLI = 1_000_000L;

    /**
     * 滑动时间窗口大小，单位：毫秒
     */
    public static final int SLIDING_WINDOW_SIZE_MILL = 30000;
    /**
     * 令牌管理过期时间，单位：毫秒
     */
    public static final int CAPTCHA_EXPIRE_TIME = 60000;
    /**
     * 令牌申请速率限制的时间窗口大小 (毫秒)
     */
    public static final long TOKEN_RATE_LIMIT_MILLIS = 10_000;

    /**
     * 时间窗口内允许的最大令牌数
     */
    public static final int MAX_TOKENS_PER_WINDOW = 3;
}
