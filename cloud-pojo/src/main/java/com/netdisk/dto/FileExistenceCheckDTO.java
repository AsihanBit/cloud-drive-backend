package com.netdisk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileExistenceCheckDTO {
    // 文件的哈希
    private String fileHash;
    // 文件的名称
    private String fileName;
    // 目标路径的 ID，用于指定文件存储的路径
    private Integer targetPathId;

    // 分片文件的哈希值
    private String chunkHash;
    // 分片的位置下标
    private Integer chunkNumber;
}
