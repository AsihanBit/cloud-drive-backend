package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.CheckFilesExistMapper;
import com.netdisk.cloudserver.service.CheckFilesExist;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.FileExistenceCheckDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.UserFiles;
import org.springframework.stereotype.Service;

@Service
public class CheckFilesExistImpl implements CheckFilesExist {

    private CheckFilesExistMapper checkFilesExistMapper;

    public CheckFilesExistImpl(CheckFilesExistMapper checkFilesExistMapper) {
        this.checkFilesExistMapper = checkFilesExistMapper;
    }

    /**
     * 文件存在判断-数据库 md5
     *
     * @param fileExistenceCheckDTO
     * @return
     */
    @Override
    public File checkFileExistByMD5(FileExistenceCheckDTO fileExistenceCheckDTO) {
        File file = checkFilesExistMapper.selectFileByMD5(fileExistenceCheckDTO.getFileHash());
        // if null 简化了
        return file;
    }

    /**
     * 文件存在判断-数据库 file_id
     *
     * @param fileId
     * @return
     */
    @Override
    public File checkFileExistByFileId(Integer fileId) {
        File file = checkFilesExistMapper.selectFileByFileId(fileId);
        return file;
    }

    /**
     * 检查用户是否拥有此文件
     *
     * @param fileId
     * @return
     */
    @Override
    public UserFiles checkUserFileExistByItemIdFileId(Integer itemId, Integer fileId) {
        Integer userId = BaseContext.getCurrentId();
        UserFiles userFiles = checkFilesExistMapper.checkUserHasFile(itemId, fileId, userId);
        return userFiles;
    }
}
