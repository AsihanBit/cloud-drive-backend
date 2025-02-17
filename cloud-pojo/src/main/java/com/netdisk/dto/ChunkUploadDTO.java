package com.netdisk.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件分块上传请求的实体类，用于封装分块上传过程中的相关参数。
 */
@Data
public class ChunkUploadDTO {
    private MultipartFile file;
    private Long start;
    private Long end;
    private Integer chunkNumber; // 此分片索引
    private Integer chunkCount; // 总分片数
    private String chunkHash; // 当前分块的哈希值
    private String fileHash; // 整个文件的哈希值

}
