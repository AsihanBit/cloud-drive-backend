package com.netdisk.cloudserver.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.CaptchaResponse;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import com.netdisk.dto.CaptchaReq;
import com.netdisk.exception.BaseException;
import com.netdisk.result.Result;
import com.netdisk.utils.CaptchaUtils;
import com.netdisk.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private ImageCaptchaApplication imageCaptchaApplication;
    private CaptchaUtils captchaUtils;

    public CaptchaController(ImageCaptchaApplication imageCaptchaApplication, CaptchaUtils captchaUtils) {
        this.imageCaptchaApplication = imageCaptchaApplication;
        this.captchaUtils = captchaUtils;
    }


    @GetMapping("/gen1")
    @ResponseBody
    public CaptchaResponse<ImageCaptchaVO> genCaptcha1(HttpServletRequest request, @RequestParam(value = "type", required = false) String type) {
        if (StringUtils.isBlank(type)) {
            type = CaptchaTypeConstant.SLIDER;
        }
        if ("RANDOM".equals(type)) {
            int i = ThreadLocalRandom.current().nextInt(0, 4);
            if (i == 0) {
                type = CaptchaTypeConstant.SLIDER;
            } else if (i == 1) {
                type = CaptchaTypeConstant.CONCAT;
            } else if (i == 2) {
                type = CaptchaTypeConstant.ROTATE;
            } else {
                type = CaptchaTypeConstant.WORD_IMAGE_CLICK;
            }

        }
        CaptchaResponse<ImageCaptchaVO> response = imageCaptchaApplication.generateCaptcha(type);
        return response;
    }

    @PostMapping("/gen")
    @ResponseBody
    public CaptchaResponse<ImageCaptchaVO> genCaptcha(HttpServletRequest request, @RequestParam(value = "type", required = false) String type) {
        log.info("生成验证码接口");
        // 检查客户端申请令牌的频率
        boolean allow = captchaUtils.checkTokenRateLimit();
        if (!allow) {
            throw new BaseException("令牌生成过于频繁，请稍后再试");
//            return Result.error("令牌生成过于频繁，请稍后再试");
        }


        String clientIp = IpUtils.getClientIp();
        log.info("获取ip测试:{}", clientIp);

        if (StringUtils.isBlank(type)) {
            type = CaptchaTypeConstant.SLIDER;
        }
        if ("RANDOM".equals(type)) {
            int i = ThreadLocalRandom.current().nextInt(0, 4);
            if (i == 0) {
                type = CaptchaTypeConstant.SLIDER;
            } else if (i == 1) {
                type = CaptchaTypeConstant.CONCAT;
            } else if (i == 2) {
                type = CaptchaTypeConstant.ROTATE;
            } else {
                type = CaptchaTypeConstant.WORD_IMAGE_CLICK;
            }

        }
        CaptchaResponse<ImageCaptchaVO> response = imageCaptchaApplication.generateCaptcha(type);
        return response;
    }


    @PostMapping("/check")
    @ResponseBody
    public ApiResponse<?> checkCaptcha(@RequestBody CaptchaReq data,
                                       HttpServletRequest request) {
        ApiResponse<?> response = imageCaptchaApplication.matching(data.getId(), data.getData());
        if (response.isSuccess()) {

            String authToken = captchaUtils.generateLoginTokenZSet();

            // 创建包含id和authToken的Map
            Map<String, String> result = new HashMap<>();
            result.put("id", data.getId());
            result.put("authToken", authToken);

            return ApiResponse.ofSuccess(result);
//            return Result.success(ApiResponse.ofSuccess(Collections.singletonMap("id", data.getId())));
        }
        return response;
//        return Result.success(response);
    }
}
