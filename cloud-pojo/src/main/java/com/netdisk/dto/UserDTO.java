package com.netdisk.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Integer userId;
    private String username;
    private String password;
    private String nickname;
    private Short age;
    private Short gender;
    private String phone;
    private String email;
    private Integer vip;
    private String avatar;
    private Long usedSpace;
    private Long totalSpace;
    private LocalDateTime registerTime;
    private LocalDateTime lastLoginTime;
    private Integer status;
}
