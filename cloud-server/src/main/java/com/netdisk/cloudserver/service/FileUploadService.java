package com.netdisk.cloudserver.service;

import com.netdisk.dto.ChunkUploadDTO;
import com.netdisk.dto.FileExistenceCheckDTO;
import com.netdisk.entity.File;

public interface FileUploadService {
    /**
     * 用户文件上传记录保存至 数据库
     *
     * @param fileExistenceCheckDTO
     */
    void userUploadFileExist(FileExistenceCheckDTO fileExistenceCheckDTO, File file);

    /**
     * 上传分片
     *
     * @param chunkUploadDTO
     */
    void uploadChunk(ChunkUploadDTO chunkUploadDTO);


}
