package com.netdisk.cloudserver.controller;

import com.netdisk.cloudserver.service.FileShareService;
import com.netdisk.dto.ShareResultDTO;
import com.netdisk.dto.UserSaveSelectedItemsDTO;
import com.netdisk.dto.SharedDTO;
import com.netdisk.dto.UserSharedItemsDTO;
import com.netdisk.enums.ShareTransferEnum;
import com.netdisk.result.Result;
import com.netdisk.utils.CipherUtils;
import com.netdisk.vo.ShareItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/share")
@Slf4j
public class FileShareController {
    // TODO 所有复杂业务加事务注解
    private FileShareService fileShareService;

    public FileShareController(FileShareService fileShareService) {
        this.fileShareService = fileShareService;
    }

    /**
     * 用户分享多个(单)文件
     *
     * @param userShareItemsDTO
     * @return
     */
    @PostMapping("/items")
    public Result<ShareResultDTO> userShareItems(@RequestBody UserSharedItemsDTO userShareItemsDTO) {
        log.info("用户分享信息 : {}", userShareItemsDTO);
        // TODO 每个业务进行合法检查: 比如此处没指定itemIds 有效期 访问次数等 抛出全局异常
        // 用户分享一系列条目
        ShareResultDTO shareTransferResultDTO = fileShareService.userShareItems(userShareItemsDTO);
        return Result.success(shareTransferResultDTO);
    }

    /**
     * 生成分享链接
     */
    @PostMapping("/generateShareLink")
    public Result<ShareResultDTO> generateShareLink(@RequestParam Integer shareId) {
        // 用户生成分享链接
        ShareResultDTO shareTransferResultDTO = fileShareService.getShareLink(shareId);
        return Result.success(shareTransferResultDTO);
    }


    /**
     * 获取用户自己分享过的文件列表
     *
     * @return
     */
    @GetMapping("/items")
    public Result<List<SharedDTO>> userShareItems() {
        // 获取用户自己分享过的文件列表
        // TODO 有效期类型改字符串
        List<SharedDTO> userOwnSharedList = fileShareService.getUserOwnSharedList();
        return Result.success(userOwnSharedList);
    }

    /**
     * 批量删除选中的分享
     *
     * @param userSharedItemsDTO
     * @return
     */
    @PostMapping("/delItems")
    public Result delShareItems(@RequestBody UserSharedItemsDTO userSharedItemsDTO) {
        log.info(userSharedItemsDTO.toString());
        fileShareService.deleteSharedItems(userSharedItemsDTO);
        return Result.success();
    }


    /**
     * 获取自己分享的文件列表
     *
     * @param shareId
     * @return
     */
    @GetMapping("/getMyShareFiles/{shareId}/{pItemId}")
    public Result<List<ShareItemVO>> getMyShareFiles(@PathVariable Integer shareId, @PathVariable Integer pItemId) {
        log.info(String.valueOf(shareId));
        // 根据 分享id 获取自己分享的文件列表
        if (pItemId == null) {
            pItemId = 0;
        }
        List<ShareItemVO> shareFiles = fileShareService.getMyShareFiles(shareId, pItemId);
        return Result.success(shareFiles);
    }

    /**
     * 获取外部分享的文件列表
     *
     * @param shareStr
     * @return
     */
    @GetMapping("/getOtherShareFiles")
    public Result<List<ShareItemVO>> getOtherShareFiles(@RequestParam String shareStr, @RequestParam Integer pItemId) {

        // 解密分享id
        Integer shareId = null;
        try {
            shareId = CipherUtils.decryptCBC(shareStr);
        } catch (Exception e) {
            // 解码错误
            log.info("解码错误");
//            throw new RuntimeException(e);
            // TODO 响应:错误的分享码格式
            return Result.success();
        }

        log.info(String.valueOf(shareId));
        // 根据 分享id 获取自己分享的文件列表
        if (pItemId == null) {
            pItemId = 0;
        }
        List<ShareItemVO> shareFiles = fileShareService.getOtherShareFiles(shareId, pItemId);
        return Result.success(shareFiles);
    }


    @PutMapping("/resetExpire/{shareId}/{expireType}/{accessLimit}")
    public Result resetShareExpire(@PathVariable Integer shareId,
                                   @PathVariable Short expireType,
                                   @PathVariable Integer accessLimit) {
        log.info(String.valueOf(shareId));
        // 重置分享的 过期时间 访问限制
        fileShareService.resetShareExpire(shareId, expireType, accessLimit);
        return Result.success();
    }

    /**
     * 使用分享链接 提取码提取文件列表
     *
     * @param shareStr
     * @param extractCode
     * @return
     */
    @GetMapping("/useShareLink")
    public Result<List<ShareItemVO>> useShareLink(@RequestParam String shareStr, @RequestParam String extractCode) {
        log.info("useShareLink: {}", String.valueOf(shareStr));
        Integer shareId = null;
        try {
            shareId = CipherUtils.decryptCBC(shareStr);
        } catch (Exception e) {
            // 解码错误
            log.info("解码错误");
//            throw new RuntimeException(e);
            // TODO 响应:错误的分享码格式
            return Result.success();
        }
        log.info(String.valueOf(shareId), extractCode);
        // TODO 前后端校验 code 格式
        List<ShareItemVO> shareItemVOList = fileShareService.useShareLink(shareId, extractCode);
        return Result.success(shareItemVOList);
    }

    /**
     * 转存文件
     *
     * @param userSaveSelectedItemsDTO
     * @return
     */
    @PostMapping("/saveSelectedItems")
    public Result saveSelectedItems(@RequestBody UserSaveSelectedItemsDTO userSaveSelectedItemsDTO) {
        log.info(userSaveSelectedItemsDTO.toString());
        log.info(userSaveSelectedItemsDTO.getFolderId().toString());

        // 转存文件
        ShareTransferEnum shareTransferEnum = fileShareService.saveSelectedItems(userSaveSelectedItemsDTO);
        return Result.success(shareTransferEnum.getMessage());
    }

}

