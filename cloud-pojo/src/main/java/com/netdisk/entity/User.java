package com.netdisk.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String nickname;
    private Short age;
    private Short gender;
    private String phone;
    private String email;
    private Integer vip; // 数据库里是 tinyint unsigned
    private String avatar;
    private Long usedSpace;
    private Long totalSpace;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private Integer status; // 数据库里是 tinyint unsigned
}
