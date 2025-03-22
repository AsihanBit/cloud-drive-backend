package com.netdisk.cloudserver.controller;

import com.netdisk.context.BaseContext;
import com.netdisk.entity.User;
import com.netdisk.result.Result;
import com.netdisk.utils.FileChunkUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/view")
@Slf4j
public class FileViewController {
    private FileChunkUtil fileChunkUtil;


    public FileViewController(FileChunkUtil fileChunkUtil) {
        this.fileChunkUtil = fileChunkUtil;
    }


    @GetMapping("/file")
    public Result viewFile() throws IOException {
        log.info("viewFile");

        // 生成文件下载 URL
        String fileUrl = "http://your-domain.com/file/download?fileId=123"; // 文件下载地址

        // 生成 kkFileView 预览链接
        String kkFileViewUrl = "http://kkfileview-domain.com/onlinePreview";
        String previewUrl = kkFileViewUrl + "?url=" + Base64.getEncoder().encodeToString(fileUrl.getBytes());

        // 返回预览链接
        Map<String, String> response = new HashMap<>();
        response.put("previewUrl", previewUrl);
        return Result.success(response);
    }

}
