package com.netdisk.dto;

import lombok.Data;

@Data
public class UserFileStatusDTO {
    private Integer itemId;
    private Integer userId;

    private Short itemType;

    private Integer fileId;
    private Short banStatus;

}
