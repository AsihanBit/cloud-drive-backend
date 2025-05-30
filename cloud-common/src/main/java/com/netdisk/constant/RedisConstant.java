package com.netdisk.constant;

public class RedisConstant {
    /**
     * 验证码
     */
    public static final String CAPTCHA = "captcha"; // 验证码
    public static final String REQUESTS = "requests"; // 请求验证码数量
    public static final String AUTH_TOKEN = "auth_token"; // 验证码通过后令牌

    /**
     * 宽带控制
     * 命名空间: bandwidth_control
     * bandwidth_control:用户id:键值
     */
    public static final String BANDWIDTH_CONTROL = "bandwidth_control"; // 宽带控制
    public static final String RATE_LIMIT = "rate_limit"; // 速率限额
    public static final String WINDOW_BYTES = "window_bytes"; // 窗口内上传的字节
    public static final String LAST_CHECK_TIME = "last_check_time"; // 上次检查时间

    public static final String CHUNK_QUOTA = "chunk_quota"; // 分片限额
    public static final String RECENT_CHUNK_COUNT = "recent_chunk_count"; // 最近分片数量

    // 文件分片
    public static final String FILE_CHUNK = "file_chunk"; // 文件分片

}
