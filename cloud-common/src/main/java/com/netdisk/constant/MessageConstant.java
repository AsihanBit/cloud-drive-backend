package com.netdisk.constant;

/**
 * 信息提示常量类
 */
public class MessageConstant {

    // 登录相关提示
    public static final String LOGIN_SUCCESS = "登录成功";
    public static final String LOGIN_FAILED = "登录失败，请检查用户名和密码";
    public static final String ACCOUNT_NOT_FOUND = "账户不存在";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_LOCKED = "账户已被锁定";
    public static final String ACCOUNT_DISABLED = "账户已被禁用";

    // 注册相关提示
    public static final String REGISTER_SUCCESS = "注册成功";
    public static final String USERNAME_DUPLICATE = "用户名已存在，请使用其他用户名"; // 用户名重复提示

    // 文件上传相关提示
    public static final String FILE_UPLOAD_SUCCESS = "文件上传成功！";
    public static final String FILE_UPLOAD_FAILED = "文件上传失败！";


    // 文件分片相关异常消息
    public static final String INVALID_INPUT_PARAMETERS = "异常输入: 文件或文件哈希值错误"; // 异常输入: 文件,哈希值为空或长度小于2
    public static final String DISK_PROPERTIES_NULL = "Disk properties cannot be null";
    public static final String TEMP_DIR_PATH_NOT_CONFIGURED = "Temporary directory path is not configured";
    public static final String FAILED_TO_STORE_CHUNK = "分片保存失败";
    public static final String FAILED_TO_CLEAR_CHUNK_TEMP_FILE = "分片临时文件清除失败";
    public static final String FAILED_TO_DELETE_CHUNK_TEMP_DIRECTORY = "分片临时文件夹删除失败";

    // 其他通用异常消息
    public static final String FILE_IS_EMPTY = "文件为空";
    public static final String FAILED_TO_CREATE_SUB_DIR = "子目录创建失败";
    public static final String CHUNK_UPLOAD_SUCCESS = "文件分片上传成功";
    public static final String CHUNK_UPLOAD_FAILED = "文件分片上传失败";
    public static final String FILE_IS_EXIST = "文件已存在, 执行秒传";
    public static final String FILE_IS_NEW = "新文件需传输";
    public static final String CHUNK_IS_EXIST = "分片已存在, 执行秒传";
    public static final String CHUNK_IS_NEW = "新分片需传输";

    // 私有构造函数，防止实例化
    private MessageConstant() {
        throw new IllegalStateException("Utility class");
    }
}
