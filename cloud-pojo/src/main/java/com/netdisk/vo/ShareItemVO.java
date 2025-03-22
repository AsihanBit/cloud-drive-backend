package com.netdisk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShareItemVO {
    private Integer shareItemId;
    private Integer shareId;
    private Integer pShareItemId;
    private Integer userId;

    private Integer itemId;
    private String itemName;
    private Short itemType;

    private Integer fileId;
    private Long fileSize;
    private String fileCover;
    private String fileExtension;

    private LocalDateTime updateTime;
}
