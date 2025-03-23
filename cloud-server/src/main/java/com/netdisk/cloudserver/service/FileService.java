package com.netdisk.cloudserver.service;

import com.netdisk.entity.File;

public interface FileService {
    /**
     * 查询文件md5值获取路径
     *
     * @param fileId
     * @return
     */
    File queryFileByFileId(Integer fileId);
}
