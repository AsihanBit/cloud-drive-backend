package com.netdisk.cloudserver.controller.user;

import cn.hutool.core.bean.BeanUtil;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.ChangePwdDTO;
import com.netdisk.dto.UserDTO;
import com.netdisk.entity.User;
import com.netdisk.result.Result;
import com.netdisk.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/account")
public class UserAccountController {
    private UserService userService;

    public UserAccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/userinfo")
    public Result<UserInfoVO> getUserInfo() {
        User user = userService.getUserById(BaseContext.getCurrentId());
        UserInfoVO userInfoVO = BeanUtil.copyProperties(user, UserInfoVO.class);
        return Result.success(userInfoVO);
    }

    @PostMapping("/modify")
    public Result<UserInfoVO> modifyUserInfo(@RequestBody UserDTO userDTO) {
        log.info("修改信息接口");
        Integer userId = BaseContext.getCurrentId();
        userDTO.setUserId(userId);
        // TODO 直接返回结果user实体 减少一次查询
        userService.updateUser(userDTO);

        User user = userService.getUserById(userId);
        user.setPassword("");
        UserInfoVO userInfoVO = BeanUtil.copyProperties(user, UserInfoVO.class);

        return Result.success(userInfoVO);
    }


    @PostMapping("/changepwd")
    public Result changepwd(@RequestBody ChangePwdDTO changePwdDTO) {
        log.info("修改密码接口");
        boolean result = userService.modifyPassword(changePwdDTO);
        return Result.success(result);
    }
}
