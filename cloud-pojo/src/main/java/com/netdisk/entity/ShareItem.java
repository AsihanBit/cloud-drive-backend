package com.netdisk.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShareItem {
    private Integer shareItemId;
    private Integer shareId;
    private Integer pShareItemId;
    private Integer userId;

    // 条目信息
    private Integer itemId;
    private String itemName;
    private Short itemType;

    // 文件的情况下 (type=1)
    private Integer fileId;
    private Long fileSize;
    private String fileCover;
    private String fileExtension;

    private LocalDateTime updateTime;
}
