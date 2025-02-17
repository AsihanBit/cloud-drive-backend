package com.netdisk.cloudserver.controller.user;

import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.dto.UserLoginDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.User;
import com.netdisk.result.Result;
import com.netdisk.utils.JwtUtil;
import com.netdisk.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserController {

    private UserService userService;
    private JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        User user = userService.userLogin(userLoginDTO);

        Integer uid = user.getUserId();
        String userToken = jwtUtil.createJwt(uid.toString());
        log.info("加密后: {}", userToken);

        Jws<Claims> parsed = jwtUtil.parseJwt(userToken);
        String parsedName = parsed.getPayload().getSubject();
        log.info("解析后: {}", parsedName);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .username(user.getUsername())
                .token(userToken)
                .build();

        return Result.success(userLoginVO);
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
