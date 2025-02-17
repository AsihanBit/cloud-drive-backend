package com.netdisk.cloudserver.service;

import com.netdisk.dto.UserLoginDTO;
import com.netdisk.dto.UserRegisterDTO;

public interface UserService {
    /**
     * 用户注册
     *
     * @param userRegisterDTO
     */
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * 用户登录
     *
     * @param userLoginDTO
     */
    void userLogin(UserLoginDTO userLoginDTO);
}
