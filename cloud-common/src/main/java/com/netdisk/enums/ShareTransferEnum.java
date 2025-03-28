package com.netdisk.enums;

import lombok.Getter;

@Getter
public enum ShareTransferEnum {

    // 定义所有可能的转存状态
    SHARE_STR_ERROR(-2, "分享码异常"),
    SHARE_EXPIRED(-1, "分享已失效"),
    SHARE_NOT_EXIST(0, "分享不存在"),
    SHARE_TRANSFER_SUCCESS(1, "转存成功"),
    SHARE_EXTRACT_CODE_ERROR(2, "提取码错误"),
    SHARE_TRANSFER_OWN_FOLDER(3, "不能转存到自己的文件夹"),
    STORAGE_LIMIT_EXCEEDED(4, "存储空间不足"),
    NETWORK_ERROR(5, "网络错误，请重试");


    private final int code;       // 状态码（可用于API返回）
    private final String message; // 错误信息（可用于前端提示）


    ShareTransferEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据状态码查找枚举（适用于反序列化）
     *
     * @param code
     * @return
     */
    public static ShareTransferEnum getShareTransferStatusByCode(int code) {
        for (ShareTransferEnum status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态码: " + code);
    }

}
