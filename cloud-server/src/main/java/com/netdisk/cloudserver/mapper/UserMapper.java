package com.netdisk.cloudserver.mapper;

import com.netdisk.entity.User;
import org.apache.ibatis.annotations.Mapper;

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
}
