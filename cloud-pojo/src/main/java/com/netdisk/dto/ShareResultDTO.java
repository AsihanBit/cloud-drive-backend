package com.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShareResultDTO {
    private Integer shareId;
    private String shareStr; // 分享码,保存了分享id. 使用AES加密
    private String shareCode; // 提取码
    private String shareLink; // 分享链接
//    private ShareStatus shareStatus; // TODO 待做
//    expireType: number
//    accessLimit: number
}
