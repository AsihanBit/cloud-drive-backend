package com.netdisk.cloudserver.controller;

import com.netdisk.cloudserver.service.FileService;
import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.context.BaseContext;
import com.netdisk.entity.User;
import com.netdisk.entity.UserFiles;
import com.netdisk.result.Result;
import com.netdisk.utils.FileChunkUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private UserFilesService userFilesService;
    private FileService fileService;


    public FileViewController(
            FileChunkUtil fileChunkUtil,
            UserFilesService userFilesService,
            FileService fileService) {
        this.fileChunkUtil = fileChunkUtil;
        this.userFilesService = userFilesService;
        this.fileService = fileService;
    }

    // 测试: 可以用的预览url生成
    @GetMapping("/previewtest")
    public String previewFileTest() {
//        Path path = fileChunkUtil.getFileCompletePath("a0aacc576a4a4f03007b3d12bd5f30d0.png");

//        String filePath = path.toString();

        // 生成文件下载 URL（假设文件下载接口为 /filedownload）
//        String originUrl = "http://127.0.0.1:8080/user/file/chunkDownload?itemId=63&fileId=306"; // 这里的 fileId 可以根据实际情况动态生成
//        String originUrl = "http://127.0.0.1:8080/user/file/downloadTest";
        String originUrl = "http://127.0.0.1:8080/user/view/filetest";

        // 指定文件名
        String fullFilename = "butter.jpg"; // 这里的文件名可以根据实际情况动态生成

        // 构建预览 URL
        String previewUrl = originUrl + "?fullfilename=" + fullFilename;

        // 对预览 URL 进行 Base64 编码
        String encodedPreviewUrl = Base64.getEncoder().encodeToString(previewUrl.getBytes());

        // 返回完整的 KKFileView 预览 URL
        return "http://localhost:8012/onlinePreview?url=" + encodedPreviewUrl;
    }

    // 测试: 可以用的文件预览
    @GetMapping("/filetest")
    public void viewFiletest(@RequestParam("fullfilename") String fullFilename, HttpServletResponse response) throws Exception {
//        File file = new File("E:\\cloudfile\\2024-steam-冬季.png");
        File file = new File("E:\\cloudfile\\" + fullFilename);

        // 检查文件是否存在
        if (!file.exists()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            log.error("文件不存在: {}", fullFilename);
            return;
        }

        // 对文件名进行URL编码
        String encodedFilename = ContentDisposition.attachment()
                .filename(file.getName(), StandardCharsets.UTF_8)
                .build()
                .toString();

        // 设置响应头
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", encodedFilename);

        // 将文件内容写入响应流
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    // 生成预览
    @GetMapping("/preview")
    public String previewFile(@RequestParam("itemId") Integer itemId) throws UnsupportedEncodingException {
        log.info("previewFile");
        // 1 检查用户是否拥有文件
        UserFiles userItem = userFilesService.checkItemOwnership(itemId);
        if (userItem == null) {
            return null;
        }
        // 2 预览url生成
        // 参数: 文件名 文件id
        String fileName = userItem.getItemName();
        Integer fileId = userItem.getFileId();

        // 生成文件构建预览 URL
        String originUrl = "http://127.0.0.1:8080/user/view/file";
//        String previewUrl = originUrl + "?fileId=" + fileId + "&fileName" + fileName + "?fullfilename=" + fileName;
//        String previewUrl = originUrl + "?fullfilename=" + fileName;
        String previewUrl = originUrl + "?fullfilename=" + fileName + "&fileId=" + fileId;

        // 对预览 URL 进行 Base64 编码
        String encodedPreviewUrl = Base64.getEncoder().encodeToString(previewUrl.getBytes());

        // 返回完整的 KKFileView 预览 URL
        return "http://localhost:8012/onlinePreview?url=" + encodedPreviewUrl;


    }


    @GetMapping("/file")
    public void viewFile(
            @RequestParam(value = "fileId", required = false) Integer fileId,
//            @RequestParam(value = "fileName", required = false) String fileName,
            HttpServletResponse response) throws Exception {
        log.info("viewFile");
        System.out.println("viewFile");
        // 1 查询文件md5值获取路径 (md5以后可以写在user_files里,冗余字段)
        com.netdisk.entity.File fileInfo = fileService.queryFileByFileId(fileId);
        String fileMd5 = fileInfo.getFileMd5();
        Path filePath = fileChunkUtil.getFileCompletePath(fileMd5);

        File file = filePath.toFile();
//        File file = new File("E:\\cloudfile\\宇宙机器人2-1.png");

        // 检查存在 filePath
        if (!Files.exists(filePath)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
//            log.error("文件不存在: {}", fileName);
            return;
        }

        // 2 对文件名进行URL编码
        String encodedFilename = ContentDisposition.attachment()
//                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();

        // 3 设置响应头
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", encodedFilename);

        // 4 将文件内容写入响应流
        // Files.newInputStream(filePath)
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("文件下载失败: {}", e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }


    }

}
