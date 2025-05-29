package com.netdisk.cloudserver.mapper;

import com.netdisk.dto.UserAccountStatusDTO;
import com.netdisk.dto.UserDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 用户注册
     *
     * @param user
     */
    void userRegister(User user);

    /**
     * 用户名查找用户
     *
     * @param username
     * @return
     */
    User queryUserByUsername(String username);

    /**
     * 根据 id 查询用户
     *
     * @param userId
     * @return
     */
    User selectUserByUserId(Integer userId);

    /**
     * 查询用户列表
     *
     * @return
     */
    List<User> selectUserInfoList();

    /**
     * 添加用户
     *
     * @param userRegisterDTO
     */
    void insertUser(UserRegisterDTO userRegisterDTO);

    /**
     * 修改账户状态
     *
     * @param userAccountStatusDTO
     */
    void updateAccountStatus(UserAccountStatusDTO userAccountStatusDTO);

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
}
