package com.netdisk.cloudserver.service;

import com.netdisk.dto.FileExistenceCheckDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.UserFiles;

public interface CheckFilesExist {
    /**
     * 文件存在判断-数据库 md5
     *
     * @param fileExistenceCheckDTO
     * @return
     */
    File checkFileExistByMD5(FileExistenceCheckDTO fileExistenceCheckDTO);

    /**
     * 文件存在判断-数据库 file_id
     *
     * @param fileId
     * @return
     */
    File checkFileExistByFileId(Integer fileId);

    /**
     * 检查用户是否拥有此文件
     *
     * @param fileId
     * @return
     */
    UserFiles checkUserFileExistByItemIdFileId(Integer itemId, Integer fileId);
}
