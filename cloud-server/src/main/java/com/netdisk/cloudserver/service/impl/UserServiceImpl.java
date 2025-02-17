package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.UserMapper;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.constant.StatusConstant;
import com.netdisk.dto.UserLoginDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.User;
import com.netdisk.exception.AccountNotFoundException;
import com.netdisk.exception.PasswordErrorException;
import com.netdisk.exception.UserAlreadyExistException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // TODO 验证用户名 密码 格式
        // 验证用户名
        User userExist = userMapper.queryUserByUsername(userRegisterDTO.getUsername());
        if (userExist != null) {
            throw new UserAlreadyExistException(MessageConstant.USERNAME_DUPLICATE);
        }
        // 验证昵称
        if (userRegisterDTO.getNickname() == null || userRegisterDTO.getNickname().isEmpty()) {
            userRegisterDTO.setNickname(userRegisterDTO.getUsername());
        }

        User newUser = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(userRegisterDTO.getPassword())
                .nickname(userRegisterDTO.getNickname())
                .vip(StatusConstant.MEMBER_LEVEL_REGULAR)
                .usedSpace(StatusConstant.Used_STORAGE_SPACE)
                .totalSpace(StatusConstant.Total_STORAGE_SPACE)
                .registerTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .status(StatusConstant.ACCOUNT_STATUS_NORMAL)
                .build();

        userMapper.userRegister(newUser);

    }

    /**
     * 用户登录
     *
     * @param userLoginDTO
     */
    @Override
    public void userLogin(UserLoginDTO userLoginDTO) {
        User user = userMapper.queryUserByUsername(userLoginDTO.getUsername());
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!user.getPassword().equals(userLoginDTO.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
    }
}
