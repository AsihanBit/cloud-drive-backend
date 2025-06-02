package com.netdisk.cloudserver.mapper;

import com.netdisk.dto.FileBanStatusDTO;
import com.netdisk.entity.File;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FileMapper {
    /**
     * 表 file 中根据 file_id 查询文件
     *
     * @param fileId
     * @return
     */
    File selectFileByFileId(Integer fileId);

    /**
     * 查询所有文件
     *
     * @return
     */
    List<File> selectAllFiles();

    /**
     * id删除文件
     *
     * @param fileId
     */
    void deleteFileByFileId(Integer fileId);

    /**
     * 启用禁用文件
     *
     * @param fileBanStatusDTO
     */
    void updateFileBanStatus(FileBanStatusDTO fileBanStatusDTO);

    /**
     * 更新文件引用次数
     *
     * @param fileId
     * @param referenceCount
     */
    void updateReferenceCount(Integer fileId, Integer referenceCount);
}
