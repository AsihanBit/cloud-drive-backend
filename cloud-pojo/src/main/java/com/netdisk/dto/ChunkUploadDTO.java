package com.netdisk.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件分块上传请求的实体类，用于封装分块上传过程中的相关参数。
 */
@Data
public class ChunkUploadDTO {
    private MultipartFile file;
    private String fileName;
    private Long start;
    private Long end;
    private Integer chunkNumber; // 此分片索引
    private Integer chunkCount; // 总分片数
    private String chunkHash; // 当前分块的哈希值
    private String fileHash; // 整个文件的哈希值
    private Integer targetPathId; // 用户上传目录

    /**
     * 传给是否是最后分片,即全部分片上传成功业务的参数:
     * chunkCount fileHash targetPathId
     */

}
