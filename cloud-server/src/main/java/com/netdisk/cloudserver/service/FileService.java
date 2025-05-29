package com.netdisk.cloudserver.service;

import com.netdisk.dto.FileBanStatusDTO;
import com.netdisk.entity.File;
import com.netdisk.vo.FileVO;

import java.util.List;

public interface FileService {
    /**
     * 查询文件md5值获取路径
     *
     * @param fileId
     * @return
     */
    File queryFileByFileId(Integer fileId);

    /**
     * 查询所有文件
     *
     * @return
     */
    List<FileVO> getAllFiles();

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
}
