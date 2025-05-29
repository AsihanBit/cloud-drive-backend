package com.netdisk.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileBanStatusDTO {
    private Integer fileId;

    private Short banStatus;

}
