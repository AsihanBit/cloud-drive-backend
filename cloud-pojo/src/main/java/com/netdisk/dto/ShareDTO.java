package com.netdisk.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareDTO {
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
    private LocalDateTime createTime;
}
