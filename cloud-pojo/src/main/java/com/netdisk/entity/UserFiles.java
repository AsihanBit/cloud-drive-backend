package com.netdisk.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFiles {
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


    /**
     * 返回文件名的后缀扩展名
     *
     * @return 文件扩展名，如果没有扩展名则返回空字符串
     */
    public String generateFileExtension() {
        if (itemName == null) {
            return "";
        }
        int lastIndex = itemName.lastIndexOf('.');
        if (lastIndex == -1 || lastIndex == itemName.length() - 1) {
            return "";
        }
        return itemName.substring(lastIndex + 1);
    }
}
