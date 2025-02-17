package com.netdisk.constant;

/**
 * 状态常量
 */
public class StatusConstant {

    // 会员等级常量
    public static final int MEMBER_LEVEL_REGULAR = 0; // 普通用户
    public static final int MEMBER_LEVEL_Vip = 1; // 会员

    // 账户状态常量
    public static final int ACCOUNT_STATUS_FROZEN = 0; // 账户初始
    public static final int ACCOUNT_STATUS_NORMAL = 1; // 账户正常
    public static final int ACCOUNT_STATUS_LOCKED = -1; // 账户冻结
    public static final int ACCOUNT_STATUS_CLOSED = -2; // 账户已关闭


    // 默认存储空间（单位：字节）
    public static final long Used_STORAGE_SPACE = 0L; // 0 字节
    public static final long Total_STORAGE_SPACE = 10L * 1024 * 1024 * 1024; // 10 GB


}
