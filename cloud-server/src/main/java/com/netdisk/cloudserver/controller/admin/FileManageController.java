package com.netdisk.cloudserver.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.netdisk.cloudserver.service.FileDownloadService;
import com.netdisk.cloudserver.service.FileService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.constant.StatusConstant;
import com.netdisk.dto.FileBanStatusDTO;
import com.netdisk.dto.ShareDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.Share;
import com.netdisk.entity.UserFiles;
import com.netdisk.exception.BaseException;
import com.netdisk.result.Result;
import com.netdisk.vo.FileVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/file")
@Slf4j
public class FileManageController {
    private FileService fileService;
    private FileDownloadService fileDownloadService;

    public FileManageController(FileService fileService,
                                FileDownloadService fileDownloadService) {
        this.fileService = fileService;
        this.fileDownloadService = fileDownloadService;
    }

    /**
     * 查询所有文件
     *
     * @return
     */
    @GetMapping("/all")
    public Result<List<FileVO>> getAllFile() {
        log.info("查询所有文件成功");
        List<FileVO> list = fileService.getAllFiles();
        return Result.success(list);
    }

    /**
     * 查询一个文件
     *
     * @return
     */
    @GetMapping("/id")
    public Result<FileVO> getFileByFileId(Integer fileId) {
        log.info("查询文件成功");
        File file = fileService.queryFileByFileId(fileId);
        FileVO fileVO = BeanUtil.copyProperties(file, FileVO.class);
        return Result.success(fileVO);
    }

    /**
     * 修改一个文件信息
     *
     * @return
     */
    @PostMapping("/modify")
    public Result modifyFileByFileId(ShareDTO shareDTO) {
        log.info("修改一个文件信息成功");
        return Result.success();
    }

    /**
     * id删除文件
     *
     * @return
     */
    @DeleteMapping("/delete")
    public Result deleteFile(Integer fileId) {
        log.info("修改一个文件成功");
        fileService.deleteFileByFileId(fileId);
        return Result.success();
    }


    /**
     * 启用禁用文件
     *
     * @return
     */
    @PostMapping("/ban")
    public Result banFileByFileId(@RequestBody FileBanStatusDTO fileBanStatusDTO) {
        log.info("启用禁用文件成功");
        fileService.updateFileBanStatus(fileBanStatusDTO);
        return Result.success();
    }


    @GetMapping("/chunkDownload") // 返回Result报警告
    public void chunkDownload(@RequestParam Integer fileId,
                              HttpServletRequest request,
                              HttpServletResponse response) throws Exception {
        log.info("下载文件id: {}", fileId);

        File file = fileService.queryFileByFileId(fileId);

        fileDownloadService.downLoadChunk(request, response, file);
        return;
//        return Result.success();
    }


}
