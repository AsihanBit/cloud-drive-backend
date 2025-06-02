package com.netdisk.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChangePwdDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
