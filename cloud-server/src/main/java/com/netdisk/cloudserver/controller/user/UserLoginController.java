package com.netdisk.cloudserver.controller.user;

import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.dto.UserLoginDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.User;
import com.netdisk.exception.BaseException;
import com.netdisk.result.Result;
import com.netdisk.utils.CaptchaUtils;
import com.netdisk.utils.JwtUtil;
import com.netdisk.vo.UserLoginVO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户接口")
@RestController
@RequestMapping("/user/user")
@Slf4j
public class UserLoginController {

    private UserService userService;
    private JwtUtil jwtUtil;
    private CaptchaUtils captchaUtils;


    public UserLoginController(
            UserService userService,
            JwtUtil jwtUtil,
            CaptchaUtils captchaUtils) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.captchaUtils = captchaUtils;
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        if (userLoginDTO.getAuthToken() == null) {
            throw new BaseException("无登录授权令牌,请完成验证");
        }
        // 滑动时间窗口验证令牌有效性
//        boolean isAuth = captchaUtils.validateLoginTokenStr(userLoginDTO.getAuthToken());
        // todo 工具类里抛出的业务异常都移出
        boolean isAuthed = captchaUtils.validateLoginTokenZSet(userLoginDTO.getAuthToken());
        if (!isAuthed) {
            throw new BaseException("登录授权令牌失效,请重新验证");
        }
        User user = userService.userLogin(userLoginDTO);

        Integer uid = user.getUserId();
        String userToken = jwtUtil.createJwt(uid.toString());
        log.info("加密后: {}", userToken);

        Jws<Claims> parsed = jwtUtil.parseJwt(userToken);
        String parsedName = parsed.getPayload().getSubject();
        log.info("解析后: {}", parsedName);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .userId(user.getUserId())
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
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        log.info("用户注册 userRegisterDTO: {}", userRegisterDTO);
        userService.register(userRegisterDTO);
        return Result.success(MessageConstant.REGISTER_SUCCESS);
    }


}
