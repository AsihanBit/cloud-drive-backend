package com.netdisk.dto;

import lombok.Data;

@Data
public class ChunkExistenceCheckDTO {
    // 用于判断分片存在性
    private String fileHash;
    private String chunkHash;
    private Integer chunkNumber;
}
