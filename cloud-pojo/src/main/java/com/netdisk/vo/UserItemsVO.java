package com.netdisk.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private Integer userId;
    private String itemName;
    private Short itemType;
    //    @JsonProperty("pId") // 显式指定 JSON 字段名为 pId
    private Integer pId;
    private Short directoryLevel;


    private Integer fileId;
    private Long fileSize;
    private String fileExtension;

    //    private LocalDateTime upLoadTime;
    private LocalDateTime updateTime;

    //    private Short recycleStatus;
    private Short banStatus;
}
