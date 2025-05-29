package com.netdisk.constant;

/**
 * 状态常量
 */
public class StatusConstant {

    // TODO 数据库外部接口可能Null时用包装类

    // 会员等级常量
    public static final int MEMBER_LEVEL_REGULAR = 0; // 普通用户
    public static final int MEMBER_LEVEL_Vip = 1; // 会员

    // 账户状态常量
    public static final int ACCOUNT_STATUS_FROZEN = 0; // 账户初始
    public static final int ACCOUNT_STATUS_NORMAL = 1; // 账户正常
    public static final int ACCOUNT_STATUS_LOCKED = 2; // 账户冻结
    public static final int ACCOUNT_STATUS_CLOSED = 3; // 账户已关闭

    // 用户文件状态常量
    public static final short ITEM_STATUS_FROZEN = 0; // 文件初始
    public static final short ITEM_STATUS_NORMAL = 1; // 文件正常
    public static final short ITEM_STATUS_LOCKED = 2; // 文件锁定

    // 分享状态常量
    public static final short SHARE_STATUS_FROZEN = 0; // 分享初始
    public static final short SHARE_STATUS_NORMAL = 1; // 分享正常
    public static final short SHARE_STATUS_LOCKED = 2; // 分享锁定


    // 默认存储空间（单位：字节）
    public static final long USED_STORAGE_SPACE = 0L; // 0 字节
    public static final long TOTAL_STORAGE_SPACE = 10L * 1024 * 1024 * 1024; // 10 GB

    // 用户转存文件相关常量
    public static final int SHARE_NOT_EXIST = 0; // 不存在这个分享
    public static final int SHARE_EXPIRED = -1; // 分享已失效
    public static final int SHARE_EXTRACT_CODE_ERROR = 2; // 提取码错误
    public static final int SHARE_TRANSFER_OWN_FOLDER = 3; // 用户转存自己的文件夹

}
