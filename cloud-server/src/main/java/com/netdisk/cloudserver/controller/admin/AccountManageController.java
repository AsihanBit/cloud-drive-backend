package com.netdisk.cloudserver.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.dto.UserAccountStatusDTO;
import com.netdisk.dto.UserDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.User;
import com.netdisk.result.Result;
import com.netdisk.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "管理用户")
@RestController
@RequestMapping("/admin/account")
@Slf4j
public class AccountManageController {
    private UserService userService;

    public AccountManageController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询用户列表
     *
     * @return
     */
    @GetMapping("/userlist")
    public Result<List<UserInfoVO>> getUserList() {
        List<UserInfoVO> userList = userService.getUserInfoList();
        return Result.success(userList);
    }

    /**
     * id查询用户
     *
     * @return
     */
    @GetMapping("/get")
    public Result<UserInfoVO> getUserById(@RequestParam Integer userId) {
        User user = userService.getUserById(userId);
        UserInfoVO userInfoVO = BeanUtil.copyProperties(user, UserInfoVO.class);
        return Result.success(userInfoVO);
    }


    /**
     * 添加用户
     *
     * @return
     */
    @PostMapping("/add")
    public Result addUser(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("添加用户成功");
        userService.addUser(userRegisterDTO);
        return Result.success();
    }

    /**
     * 删除用户账户
     *
     * @return
     */
    @DeleteMapping("/delete")
    public Result delUser(@RequestParam Integer userId) {
        log.info("删除用户账户成功");
        userService.deleteUserAccountByUserId(userId);
        return Result.success();
    }

    /**
     * 修改用户信息
     *
     * @return
     */
    @PostMapping("/modify")
    public Result<UserInfoVO> updateUserInfo(@RequestBody UserDTO userDTO) {
        log.info("修改用户成功");
        userService.updateUser(userDTO);
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userDTO, UserInfoVO.class);
        return Result.success(userInfoVO);
    }


    /**
     * 修改账户状态
     *
     * @return
     */
    @PostMapping("/ban")
    public Result<UserInfoVO> modifyAccountStatus(@RequestBody UserAccountStatusDTO userAccountStatusDTO) {
        log.info("修改账户状态成功");
        userService.modifyUserAccountStatus(userAccountStatusDTO);
        return Result.success();
    }


}
