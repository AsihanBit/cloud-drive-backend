package com.netdisk.cloudserver.controller;

import com.netdisk.cloudserver.service.CheckFilesExist;
import com.netdisk.cloudserver.service.FileUploadService;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.constant.StatusConstant;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.ChunkUploadDTO;
import com.netdisk.dto.FileExistenceCheckDTO;
import com.netdisk.entity.File;
import com.netdisk.exception.BaseException;
import com.netdisk.result.Result;
import com.netdisk.utils.FileChunkUtil;
import com.netdisk.utils.RedisUtil;
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

    private UserService userService;
    private FileChunkUtil fileChunkUtil;
    private FileUploadService fileUploadService;
    private CheckFilesExist checkFilesExist;
    private RedisUtil redisUtil;

    public FileUploadController(
            UserService userService,
            FileChunkUtil fileChunkUtil,
            FileUploadService fileUploadService,
            CheckFilesExist checkFilesExist,
            RedisUtil redisUtil) {
        this.userService = userService;
        this.fileChunkUtil = fileChunkUtil;
        this.fileUploadService = fileUploadService;
        this.checkFilesExist = checkFilesExist;
        this.redisUtil = redisUtil;
    }

    @PostMapping("/upload")
    public Result uploadFile(MultipartFile file) {
        log.info("文件上传接口: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            log.info("文件为空");
        }

        return Result.success(MessageConstant.FILE_UPLOAD_SUCCESS);
    }
    // TODO return 统一都放在控制层里

    @Operation(summary = "分片上传")
    @PostMapping("/chunkUpload")
    public Result chunkUploadFile(ChunkUploadDTO chunkUploadDTO) {
        // 1. 判断剩余空间是否足够


        // 2. 上传每个分片
        fileUploadService.uploadChunk(chunkUploadDTO);

        log.info("整个文件哈希值: {}", chunkUploadDTO.getFileHash());
        log.info("参数: {} {} {} {} {} {}",
                chunkUploadDTO.getFile().getOriginalFilename(),
                chunkUploadDTO.getStart(),
                chunkUploadDTO.getEnd(),
                chunkUploadDTO.getChunkCount(),
                chunkUploadDTO.getChunkHash(),
                chunkUploadDTO.getFileHash());
        log.info("分片: {} / {}", chunkUploadDTO.getChunkNumber(), chunkUploadDTO.getChunkCount());
        log.info("目标路径id: {}", chunkUploadDTO.getTargetPathId());

//        fileChunkUtil.storeChunk(
//                chunkUploadDTO.getFile(),
//                chunkUploadDTO.getFileHash(),
//                chunkUploadDTO.getChunkNumber(),
//                chunkUploadDTO.getChunkHash());
        return Result.success();
    }


    @Operation(summary = "分片上传至用户指定目录")
    @PostMapping("/chunkUpload/test")
    public Result uploadChunkToUserPath(ChunkUploadDTO chunkUploadDTO, Integer path) {

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
    @PostMapping("/fileIsExist")
    public Result fileIsExist(@RequestBody FileExistenceCheckDTO fileExistenceCheckDTO) {
        // 需要: 文件名 pid (file_id file_size file_cover) [file_extension]
        // @RequestParam String fileHash, @RequestParam Integer targetPathId

        // 1. 判断剩余空间是否足够可用
        boolean spaceEnough = userService.checkSpaceEnough(fileExistenceCheckDTO.getFileSize());
        if (!spaceEnough) {
//            throw new BaseException("账户剩余可用空间不足");
            return Result.success(2);
        }

        // 2. 文件存在判断-数据库
        File file = checkFilesExist.checkFileExistByMD5(fileExistenceCheckDTO);
        if (file == null) {
            return Result.success(0); // 文件不存在 上传
        }

        // 3. 文件存在 判断是否封禁
        if (file.getBanStatus() == StatusConstant.ITEM_STATUS_LOCKED) {
//            throw new BaseException("该文件可能涉及违禁内容,当前不可上传");
            return Result.success(3);
        }

        fileUploadService.userUploadFileExist(fileExistenceCheckDTO, file);
        return Result.success(1); // TODO 这里都换成常量 字符/数字/实体/枚举

        /*
        // 文件存在判断-本地存储
        boolean isExist = fileChunkUtil.checkFileExistsLS(fileExistenceCheckDTO.getFileHash());
        if (isExist) {
            log.info("文件已存在");
            // 用户文件上传记录保存至 数据库
            fileUploadService.userUploadFileIsExist(fileExistenceCheckDTO);
        } else {
            log.info("文件是新文件");
        }
        return Result.success(isExist);
        */
    }

    @Operation(summary = "分片存在性判断")
    @GetMapping("/chunkIsExist")
    public Result chunkIsExist(@RequestParam String fileHash, @RequestParam String chunkHash, @RequestParam Integer chunkNumber) {
        // 分片存在性-本地存储式判断
//        boolean isExist = fileChunkUtil.checkChunkExistsLS(fileHash, chunkHash, chunkNumber);
        Integer userId = BaseContext.getCurrentId();
        boolean isExist = fileChunkUtil.checkChunkExists(userId, fileHash, chunkNumber);
        if (isExist) {
            log.info("分片已存在");
            // 存储式-分片上传记录保存至 redis
        } else {
            log.info("分片是新文件");
        }
        // TODO 如果全部分片都在 redis中 存在,清空redis,未合并的话,执行合并,保存es和mysql
//        MergeFileResult mergeFileResult = fileChunkUtil.mergeChunks(fileHash);
        // 清除分片redis
//      如果所有分片都在:redisUtil.deleteAllChunk(userId, fileHash);

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
/// /        log.info("文件上传接口");
//        log.info("整个文件哈希值: {}", fileHash);
//        log.info("参数: {} {} {} {} {}  分片: {} / {}",
//                file.getOriginalFilename(), file.getSize(), start, end, chunkHash, chunkNumber, chunkCount);
//        if (file.isEmpty()) {
//            log.info("文件为空");
//        }
//        fileMergeUtil.storeChunk(file, fileHash, chunkNumber, chunkHash);
//        return Result.success();
//    }
