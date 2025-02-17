package com.netdisk.cloudserver.controller.user;

import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.dto.UserLoginDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.User;
import com.netdisk.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/account")
@Slf4j
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDTO userLoginDTO) {
        userService.userLogin(userLoginDTO);

        return Result.success(MessageConstant.LOGIN_SUCCESS);
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册 userRegisterDTO: {}", userRegisterDTO);
        userService.register(userRegisterDTO);
        return Result.success(MessageConstant.REGISTER_SUCCESS);
    }
}
