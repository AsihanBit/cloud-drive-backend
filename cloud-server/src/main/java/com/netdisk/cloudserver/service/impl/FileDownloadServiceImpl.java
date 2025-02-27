package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.service.FileDownloadService;
import com.netdisk.entity.File;
import com.netdisk.utils.FileChunkUtil;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

@Service
public class FileDownloadServiceImpl implements FileDownloadService {
    private FileChunkUtil fileChunkUtil;

    public FileDownloadServiceImpl(FileChunkUtil fileChunkUtil) {
        this.fileChunkUtil = fileChunkUtil;
    }

    /**
     * 下载分片
     *
     * @param request
     * @param response
     * @param file
     */
    @Override
    public void downLoadChunk(HttpServletRequest request, HttpServletResponse response, File file) {
        Path filePath = fileChunkUtil.getFileCompletePath(file.getFileMd5());
        java.io.File fileLocal = filePath.toFile();
        // 1. 解析 Range 请求头
        long fileSize = file.getFileSize();
        long start = 0;
        long end = fileSize - 1;
        String rangeHeader = request.getHeader("Range");

        // 处理 Range 请求
        if (rangeHeader != null) {
            try {
                // 解析格式：bytes=0-1023
                String[] ranges = rangeHeader.substring(6).split("-");
                start = Long.parseLong(ranges[0]);
                end = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileSize - 1;
            } catch (Exception e) {
                response.setHeader("Content-Range", "bytes */" + fileSize);
//                response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid Range Header");
                return;
            }

            // 校验范围有效性
            if (start >= fileSize || end >= fileSize || start > end) {
                response.setHeader("Content-Range", "bytes */" + fileSize);
//                response.sendError(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value(), "Invalid Range");
                return;
            }
        }

        // 3. 设置响应头（必须在写入数据前设置！）
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=233.txt");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(end - start + 1));

        if (rangeHeader != null) {
            response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
            response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
        }

        // 4. 流式传输文件内容
        try (RandomAccessFile raf = new RandomAccessFile(fileLocal, "r");
             ServletOutputStream out = response.getOutputStream()) {

            raf.seek(start);
            long remaining = end - start + 1;
            byte[] buffer = new byte[4096];

            while (remaining > 0) {
                int readSize = (int) Math.min(buffer.length, remaining);
                readSize = raf.read(buffer, 0, readSize);
                if (readSize == -1) break;

                out.write(buffer, 0, readSize);
                remaining -= readSize;

                // 人为增加延迟（例如 100 毫秒）
//                try {
//                    Thread.sleep(10); // 延迟 100 毫秒
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

            }
        } catch (IOException e) {
            // 连接中断是正常现象，无需处理
            if (!e.getClass().getName().contains("ClientAbortException")) {
//                throw e;
            }
        }

    }
}
