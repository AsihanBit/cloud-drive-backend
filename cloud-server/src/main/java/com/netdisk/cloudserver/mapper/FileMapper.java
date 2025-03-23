package com.netdisk.cloudserver.mapper;

import com.netdisk.entity.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {
    /**
     * 表 file 中根据 file_id 查询文件
     *
     * @param fileId
     * @return
     */
    File selectFileByFileId(Integer fileId);
}
