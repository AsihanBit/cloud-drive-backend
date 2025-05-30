package com.netdisk.dto;


import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import lombok.Data;

@Data
public class CaptchaReq {
    private String id;
    private ImageCaptchaTrack data;

}
