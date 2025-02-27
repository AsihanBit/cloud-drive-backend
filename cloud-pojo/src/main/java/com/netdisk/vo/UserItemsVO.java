package com.netdisk.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserItemsVO {
    private Integer itemId;
    private String itemName;
    private Short itemType;
    //    private Integer pId;
    private Short directoryLevel;


    private Integer fileId;
    private Long fileSize;
    private String fileExtension;

    private LocalDateTime updateTime;
}
