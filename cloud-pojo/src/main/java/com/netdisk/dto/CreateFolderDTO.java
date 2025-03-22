package com.netdisk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateFolderDTO {
    @JsonProperty("pId") // 显式指定 JSON 字段名为 pId
    private Integer pId;
    private String folderName;
}
