package com.netdisk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShareVO {
    // 冗余字段 用户名 昵称
    // TODO 分享条目数  总文件数
    private Integer shareId;
    private String shareCode;
    private Integer userId;
    private String username;
    private String nickname;
    private Short expireType;
    private LocalDateTime expireTime;
    private Integer accessCount;
    private Integer accessLimit;
    private Short shareStatus;
    private Short banStatus;
    private LocalDateTime createTime;
}
