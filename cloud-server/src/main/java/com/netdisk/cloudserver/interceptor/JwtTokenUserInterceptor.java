package com.netdisk.cloudserver.interceptor;

import com.netdisk.context.BaseContext;
import com.netdisk.properties.JwtProperties;
import com.netdisk.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户端 jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    private JwtProperties jwtProperties;
    private JwtUtil jwtUtil;

    public JwtTokenUserInterceptor(JwtProperties jwtProperties, JwtUtil jwtUtil) {
        this.jwtProperties = jwtProperties;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String authHeader = request.getHeader(jwtProperties.getUserTokenName());
        String token = "";
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // 提取 JWT
            // 校验 JWT
//            log.info("authHeader不为空");
        } else {
            log.info("authHeader为空");
        }

        //2、校验令牌
        try {
            log.info("jwt校验:{}", token);
            Jws<Claims> jwsClaims = jwtUtil.parseJwt(token);
            Integer uid = Integer.valueOf(jwsClaims.getPayload().getSubject());
            log.info("拦截器: 用户id {}", uid);
            BaseContext.setCurrentId(uid);
            // 3.通过, 放行
            return true;
        } catch (Exception e) {
            // 4.不通过,响应401状态码
            log.info("用户令牌未通过校验");
            response.setStatus(401);
            return false;
        }

    }
}
