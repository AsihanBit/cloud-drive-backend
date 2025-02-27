package com.netdisk.cloudserver.mapper;

import com.netdisk.entity.File;
import com.netdisk.entity.UserFiles;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CheckFilesExistMapper {
    /**
     * MD5查询文件
     *
     * @param fileHash
     * @return
     */
    File selectFileByMD5(String fileHash);

    /**
     * file_id 查询文件
     *
     * @param fileId
     * @return
     */
    File selectFileByFileId(Integer fileId);

    /**
     * 检查用户是否拥有文件
     *
     * @param itemId
     * @param fileId
     * @param userId
     * @return
     */
    UserFiles checkUserHasFile(Integer itemId, Integer fileId, Integer userId);
}
