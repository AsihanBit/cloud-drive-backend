package com.netdisk.cloudserver.controller;

import com.netdisk.constant.MessageConstant;
import com.netdisk.result.Result;
import com.netdisk.utils.FileMergeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private FileMergeUtil fileMergeUtil;

    public FileController(FileMergeUtil fileMergeUtil) {
        this.fileMergeUtil = fileMergeUtil;
    }

    @PostMapping("/upload")
    public Result uploadFile(MultipartFile file) {
        log.info("文件上传接口: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            log.info("文件为空");
        }

        return Result.success(MessageConstant.FILE_UPLOAD_SUCCESS);
    }

    @PostMapping("/chunkUpload")
    public Result chunkUploadFile(@RequestParam("file") MultipartFile file,
                                  @RequestParam("start") long start,
                                  @RequestParam("end") long end,
                                  @RequestParam("chunkNumber") Integer chunkNumber,
                                  @RequestParam("chunkHash") String chunkHash,
                                  @RequestParam("fileHash") String fileHash) {
//        log.info("文件上传接口");
        log.info("整个文件哈希值: {}", fileHash);
        log.info("参数: {} {} {} {} {} {}", file.getOriginalFilename(), file.getSize(), start, end, chunkNumber, chunkHash);
        if (file.isEmpty()) {
            log.info("文件为空");
        }
        fileMergeUtil.storeChunk(file, fileHash, chunkNumber, chunkHash);

        return Result.success();
    }
}
