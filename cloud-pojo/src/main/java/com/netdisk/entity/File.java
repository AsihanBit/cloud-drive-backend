package com.netdisk.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class File {
    private Integer fileId;
    private String fileMd5;
    private String storageLocation;
    private Long fileSize;
    private String fileCover;
    private Integer referenceCount;
    private Integer userId;
    private Short transcodeStatus;
    private Short banStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
