package com.netdisk.constant;

/**
 * 文件常量
 */
public class FileConstant {
    /**
     * 基本单位常量 单位byte
     */
    // 左移表达
    public static final long KB = 1L << 10;  // 2^10 = 1024
    public static final long MB = 1L << 20;  // 2^20 = 1,048,576
    public static final long GB = 1L << 30;  // 2^30 = 1,073,741,824
    /**
     * public static final long KB = 1024L;
     * public static final long MB = 1024 * 1024L;
     * public static final long GB = 1024 * 1024 * 1024L;
     */

    // 文件分片大小 1M
    public static final long CHUNK_SIZE = 1024 * 1024L; // 比左移直观

    // 限速触发阈值常量
    /**
     * 文件大小限速阈值：当传输文件大小超过此值时触发限速检查 (2MB)
     */
    public static final long BANDWIDTH_LIMIT_FILE_SIZE_THRESHOLD = MB << 1; // 2 * MB

    /**
     * 分片数量限速阈值：当已上传分片数量超过此值时触发限速检查
     */
    public static final int BANDWIDTH_LIMIT_CHUNK_COUNT_THRESHOLD = 10;

    /**
     * 默认带宽限制 (单位/s)
     */
    public static final int DEFAULT_MAX_BANDWIDTH_RATE = 1024;


}
