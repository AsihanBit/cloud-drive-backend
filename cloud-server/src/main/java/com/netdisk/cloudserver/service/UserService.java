package com.netdisk.cloudserver.service;

import cn.hutool.system.UserInfo;
import com.netdisk.dto.*;
import com.netdisk.entity.File;
import com.netdisk.entity.User;
import com.netdisk.vo.UserInfoVO;

import java.util.List;

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
    User userLogin(UserLoginDTO userLoginDTO);

    /**
     * 查询用户列表
     *
     * @return
     */
    List<UserInfoVO> getUserInfoList();

    /**
     * id查询用户
     *
     * @param userId
     * @return
     */
    User getUserById(Integer userId);

    /**
     * 添加用户
     *
     * @param userRegisterDTO
     */
    void addUser(UserRegisterDTO userRegisterDTO);

    /**
     * 修改账户状态
     *
     * @param userAccountStatusDTO
     */
    void modifyUserAccountStatus(UserAccountStatusDTO userAccountStatusDTO);

    /**
     * 删除用户账户
     *
     * @param userId
     */
    void deleteUserAccountByUserId(Integer userId);

    /**
     * 修改用户信息
     *
     * @param userDTO
     */
    void updateUser(UserDTO userDTO);

    /**
     * 判断剩余空间是否足够可用
     *
     * @param fileSize
     * @return
     */
    boolean checkSpaceEnough(Long fileSize);

    /**
     * 修改密码接口
     *
     * @param changePwdDTO
     * @return
     */
    boolean modifyPassword(ChangePwdDTO changePwdDTO);
}
