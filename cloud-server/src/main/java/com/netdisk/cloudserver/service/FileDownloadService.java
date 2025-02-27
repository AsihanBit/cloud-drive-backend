package com.netdisk.cloudserver.service;

import com.netdisk.entity.File;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface FileDownloadService {
    /**
     * 下载分片
     *
     * @param request
     * @param response
     * @param file
     */
    void downLoadChunk(HttpServletRequest request, HttpServletResponse response, File file);
}
