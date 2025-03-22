package com.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSharedDTO {
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
