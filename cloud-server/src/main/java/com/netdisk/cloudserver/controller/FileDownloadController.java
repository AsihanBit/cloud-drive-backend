package com.netdisk.cloudserver.controller;

import com.netdisk.cloudserver.service.CheckFilesExist;
import com.netdisk.cloudserver.service.FileDownloadService;
import com.netdisk.entity.UserFiles;
import com.netdisk.result.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/user/file")
@Slf4j
public class FileDownloadController {
    // TODO user/file多个controller合并

    private FileDownloadService fileDownloadService;
    private CheckFilesExist checkFilesExist;

    public FileDownloadController(FileDownloadService fileDownloadService, CheckFilesExist checkFilesExist) {
        this.fileDownloadService = fileDownloadService;
        this.checkFilesExist = checkFilesExist;
    }

    @GetMapping("/downloadTest")
    public void downloadFile1(@RequestParam("fullfilename") String fullFilename, HttpServletRequest request, HttpServletResponse response) throws Exception {
        File file = new File("E:\\cloudfile\\2024-steam-冬季.png");
        response.setCharacterEncoding("utf-8");
        InputStream is = null;
        OutputStream os = null;

        try {
            // 分片
            Long fsize = file.length();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("fSize", fsize + "");
            response.setHeader("fName", file.getName());

            long pos = 0, last = fsize - 1, sum = 0;
            if (null != request.getHeader("Range")) {
                // 需要分片
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                // 分片信息
                String numRange = request.getHeader("Range").replaceAll("bytes=", "");
//                numRange = numRange.substring(numRange.indexOf("=") + 1);

                String[] strRange = numRange.split("-");
                if (strRange.length == 2) {
                    pos = Long.parseLong(strRange[0].trim());
                    last = Long.parseLong(strRange[1].trim());
                    if (last > fsize - 1) {
                        last = fsize - 1;
                    }
                } else {
                    pos = Long.parseLong(numRange.replaceAll("-", "").trim());
                }
            }
            long rangeLength = last - pos + 1;
//            String contentType = request.getHeader("Content-Type");
//            response.setContentType(contentType);

            String contentRange = new StringBuffer("bytes=" + pos + "-" + last + "/" + fsize).toString();
            response.setHeader("Content-Range", contentRange);
            response.setHeader("Content-Length", String.valueOf(rangeLength));

            os = new BufferedOutputStream(response.getOutputStream());
            is = new BufferedInputStream(new FileInputStream(file));
            is.skip(pos); // 也可randomaccess
            byte[] buffer = new byte[1024];
            int length = 0;
//            while ((length = is.read(buffer)) != -1) {}

            while (sum < rangeLength) {
                length = is.read(buffer, 0, (rangeLength - sum) <= buffer.length ? ((int) (rangeLength - sum)) : buffer.length);
                sum += length;
                os.write(buffer, 0, length);
            }
            System.out.println("下载完成");

        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }

    }


    @GetMapping("/downloadChunk")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String filePath = "E:\\cloudfile\\233.txt";
        String filePath = "E:\\cloudfile\\l月外网搬运.zip";
        File file = new File(filePath);

        // 1. 校验文件是否存在
        if (!file.exists() || !file.isFile()) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "File not found");
            return;
        }

        // 2. 解析 Range 请求头
        long fileSize = file.length();
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
                response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid Range Header");
                return;
            }

            // 校验范围有效性
            if (start >= fileSize || end >= fileSize || start > end) {
                response.setHeader("Content-Range", "bytes */" + fileSize);
                response.sendError(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value(), "Invalid Range");
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
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
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
                throw e;
            }
        }
    }

    @GetMapping("/chunkDownload") // 返回Result报警告
    public void chunkDownload(@RequestParam Integer itemId,
                              @RequestParam Integer fileId,
                              HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        log.info("下载文件id: {}", fileId);
        // 1 检查file中是否存在
        com.netdisk.entity.File file = checkFilesExist.checkFileExistByFileId(fileId);
        if (file == null) {
            // 待做 自定义异常
            response.sendError(HttpStatus.NOT_FOUND.value(), "File not found");
            return;
//            return Result.error("文件不存在");
        }
        // 2 检查用户是否拥有此文件
        UserFiles userFile = checkFilesExist.checkUserFileExistByItemIdFileId(itemId, fileId);
        if (userFile == null) {
            // 待做 自定义异常
            response.sendError(HttpStatus.NOT_FOUND.value(), "File not found");
            return;
//            return Result.error("用户对文件没权限");
        }
        // 3 保存文件: 待做:根据用户的扩展名返回(其实前端已完成)
        fileDownloadService.downLoadChunk(request, response, file);


        return;
//        return Result.success();
    }


}

// TODO 上传下载记录表