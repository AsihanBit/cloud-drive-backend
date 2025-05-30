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
     * 验证码 滑动时间窗口大小，单位：毫秒
     */
    public static final int SLIDING_WINDOW_SIZE_MILL = 5000;
}
