package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.FileMapper;
import com.netdisk.cloudserver.service.FileService;
import com.netdisk.entity.File;
import org.springframework.stereotype.Service;

@Service
public class FileServiceImpl implements FileService {
    private FileMapper fileMapper;

    public FileServiceImpl(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    /**
     * 表 file 中根据 file_id 查询文件
     *
     * @param fileId
     * @return
     */
    @Override
    public File queryFileByFileId(Integer fileId) {
        File file = fileMapper.selectFileByFileId(fileId);
        return file;
    }
}
