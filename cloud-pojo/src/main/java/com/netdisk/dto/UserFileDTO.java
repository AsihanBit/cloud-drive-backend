package com.netdisk.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFileDTO {
    private Integer ItemId;
    private Integer userId;
    private String itemName;
    private Short itemType;
    private Integer pId;
    private Short directoryLevel;

    private Integer fileId;
    private Long fileSize;
    private String fileCover;
    private String fileExtension;

    private LocalDateTime upLoadTime;
    private LocalDateTime updateTime;
    private Short recycleStatus;
    private Short banStatus;

}
