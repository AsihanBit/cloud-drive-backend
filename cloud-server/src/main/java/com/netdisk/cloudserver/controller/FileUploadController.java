package com.netdisk.cloudserver.controller;

import com.netdisk.cloudserver.service.FileUploadService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.dto.ChunkUploadDTO;
import com.netdisk.result.Result;
import com.netdisk.utils.FileChunkUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "文件上传接口")
@RestController
@RequestMapping("/user/file")
public class FileUploadController {
    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private FileChunkUtil fileChunkUtil;
    private FileUploadService fileUploadService;

    public FileUploadController(FileChunkUtil fileChunkUtil, FileUploadService fileUploadService) {
        this.fileChunkUtil = fileChunkUtil;
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/upload")
    public Result uploadFile(MultipartFile file) {
        log.info("文件上传接口: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            log.info("文件为空");
        }

        return Result.success(MessageConstant.FILE_UPLOAD_SUCCESS);
    }

    @Operation(summary = "分片上传")
    @PostMapping("/chunkUpload")
    public Result chunkUploadFile(ChunkUploadDTO chunkUploadDTO) {
        log.info("整个文件哈希值: {}", chunkUploadDTO.getFileHash());
        log.info("参数: {} {} {} {} {} {}",
                chunkUploadDTO.getFile().getOriginalFilename(),
                chunkUploadDTO.getStart(),
                chunkUploadDTO.getEnd(),
                chunkUploadDTO.getChunkCount(),
                chunkUploadDTO.getChunkHash(),
                chunkUploadDTO.getFileHash());
        log.info("分片: {} / {}", chunkUploadDTO.getChunkNumber(), chunkUploadDTO.getChunkCount());

//        fileUploadService.uploadChunk(chunkUploadDTO);
        fileChunkUtil.storeChunk(
                chunkUploadDTO.getFile(),
                chunkUploadDTO.getFileHash(),
                chunkUploadDTO.getChunkNumber(),
                chunkUploadDTO.getChunkHash());
        // 这里可以添加文件分块处理的逻辑
        return Result.success();
    }

    // 合并分片 测试
    @Operation(summary = "合并分片")
    @PostMapping("/mergeTest")
    public Result mergeTest() throws IOException {
        log.info("合并分片测试:");
        fileChunkUtil.mergeChunks("f3b59b33ae0a914774649e705ce75450"); // 压缩包
//        fileMergeUtil.mergeChunks("9bde7af29034ed1b0c7bf1730fb3e2b9"); // 视频
//        fileMergeUtil.mergeChunks("a0aacc576a4a4f03007b3d12bd5f30d0"); // steam冬促图片
//        fileChunkUtil.mergeChunks("4e9c7f46c0cc45912a25d58c01e02235"); // butter图片
//        fileMergeUtil.mergeChunks("1721076083742c27273c458476d5e3a0"); // 一个txt

        
        return Result.success();
    }

    @Operation(summary = "文件存在性判断")
    @GetMapping("/fileIsExist")
    public Result fileIsExist(@RequestParam String fileHash) {
        boolean isExist = fileChunkUtil.checkFileExists(fileHash);
        if (isExist) {
            log.info("文件已存在");
        } else {
            log.info("文件是新文件");
        }
        return Result.success(isExist);
    }

    @Operation(summary = "分片存在性判断")
    @GetMapping("/chunkIsExist")
    public Result chunkIsExist(@RequestParam String fileHash, @RequestParam String chunkHash, @RequestParam Integer chunkNumber) {
        boolean isExist = fileChunkUtil.checkChunkExists(fileHash, chunkHash, chunkNumber);
        if (isExist) {
            log.info("分片已存在");
        } else {
            log.info("分片是新文件");
        }
        return Result.success(isExist);
    }
}

//    @PostMapping("/chunkUpload")
//    public Result chunkUploadFile(@RequestParam("file") MultipartFile file,
//                                  @RequestParam("start") Long start,
//                                  @RequestParam("end") Long end,
//                                  @RequestParam("chunkNumber") Integer chunkNumber,
//                                  @RequestParam("chunkCount") Integer chunkCount,
//                                  @RequestParam("chunkHash") String chunkHash,
//                                  @RequestParam("fileHash") String fileHash) {
////        log.info("文件上传接口");
//        log.info("整个文件哈希值: {}", fileHash);
//        log.info("参数: {} {} {} {} {}  分片: {} / {}",
//                file.getOriginalFilename(), file.getSize(), start, end, chunkHash, chunkNumber, chunkCount);
//        if (file.isEmpty()) {
//            log.info("文件为空");
//        }
//        fileMergeUtil.storeChunk(file, fileHash, chunkNumber, chunkHash);
//        return Result.success();
//    }
