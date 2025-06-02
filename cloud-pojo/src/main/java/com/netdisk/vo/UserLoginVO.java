package com.netdisk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO {
    private Integer userId;
    private String username;
    private String token;

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
