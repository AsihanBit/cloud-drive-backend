package com.netdisk.cloudserver.mapper;

import com.netdisk.entity.File;
import com.netdisk.entity.UserFiles;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileUploadMapper {
    /**
     * 用户文件上传记录保存至 数据库
     *
     * @param userFile
     */
    Integer insertUserFile(UserFiles userFile);

    /**
     * file 中存储新文件信息
     *
     * @param newFile
     */
    Integer insertNewFile(File newFile);

    /**
     * 获取父文件夹PId的层级DirectoryLevel
     *
     * @param targetPathId
     * @return
     */
    Short getPIdDirectoryLevel(Integer targetPathId);


}
