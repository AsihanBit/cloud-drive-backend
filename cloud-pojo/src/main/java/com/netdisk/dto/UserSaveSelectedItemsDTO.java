package com.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveSelectedItemsDTO {
    private Integer shareId;
    private String extractCode;
    // 转存目标文件夹
    private Integer folderId; // pItemId 不行,只收到null?
    private List<Integer> selectedItemIds;
}
