package com.netdisk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileVO {
    private Integer fileId;
    private String fileMd5;
    private String storageLocation;
    private Long fileSize;
    private String fileCover;
    private Integer referenceCount;
    private Integer userId;
    private String userExtension;
    private Short transcodeStatus;
    private Short banStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
