package com.netdisk.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileChunkDTO {
    private MultipartFile file;
    private long start;
    private long end;
    private int index;
    private String hash;
}
