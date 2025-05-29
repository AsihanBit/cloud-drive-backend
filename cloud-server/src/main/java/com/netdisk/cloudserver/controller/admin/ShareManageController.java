package com.netdisk.cloudserver.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.netdisk.cloudserver.service.FileShareService;
import com.netdisk.dto.ShareBanStatusDTO;
import com.netdisk.dto.ShareDTO;
import com.netdisk.entity.Share;
import com.netdisk.entity.ShareItem;
import com.netdisk.result.Result;
import com.netdisk.vo.ShareItemVO;
import com.netdisk.vo.ShareVO;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/share")
@Slf4j
public class ShareManageController {
    private FileShareService fileShareService;

    public ShareManageController(FileShareService fileShareService) {
        this.fileShareService = fileShareService;
    }

    /**
     * 查询所有分享
     *
     * @return
     */
    @GetMapping("/all")
    public Result<List<ShareVO>> getAllShare() {
        log.info("查询所有分享成功");
        List<ShareVO> list = fileShareService.getAllShare();
        return Result.success(list);
    }

    /**
     * 分享id查询分享
     *
     * @return
     */
    @GetMapping("/searchByShareId")
    public Result<ShareVO> getShareByShareId(@RequestParam(required = false) Integer shareId) {
        log.info("shareId查询分享成功");
        Share share = fileShareService.getShareById(shareId);
        ShareVO shareVO = BeanUtil.copyProperties(share, ShareVO.class);
        return Result.success(shareVO);
    }


    /**
     * 用户id查询分享
     *
     * @return
     */
    @GetMapping("/searchByUserId")
    public Result<List<ShareVO>> getShareByUserId(@RequestParam(required = false) Integer userId) {
        log.info("userId查询分享成功");
        List<Share> shareList = fileShareService.getShareByUserId(userId);
        List<ShareVO> shareVOS = BeanUtil.copyToList(shareList, ShareVO.class);
        return Result.success(shareVOS);
    }


    /**
     * id查询分享的每个条目
     *
     * @return
     */
    @GetMapping("/allitems")
    public Result<List<ShareItemVO>> getShareItemsByShareId(Integer shareId) {
        log.info("id查询分享成功");
        List<ShareItem> list = fileShareService.getShareItemsByShareId(shareId);
        List<ShareItemVO> shareItemVOS = BeanUtil.copyToList(list, ShareItemVO.class);
        return Result.success(shareItemVOS);
    }

    /**
     * id删除分享
     *
     * @return
     */
    @DeleteMapping("/delete")
    public Result deleteShare(@RequestParam Integer shareId) {
        log.info("id删除分享成功");
        fileShareService.deleteShareByShareId(shareId);
        return Result.success();
    }

    /**
     * 修改分享
     *
     * @return
     */
    @PostMapping("/modify")
    public Result modifyShare(ShareDTO shareDTO) {
        log.info("修改分享成功");
        return Result.success();
    }


    /**
     * 重置分享
     *
     * @return
     */
    @PutMapping("/resetExpire/{shareId}/{expireType}/{accessLimit}")
    public Result resetShare(@PathVariable Integer shareId,
                             @PathVariable Short expireType,
                             @PathVariable Integer accessLimit) {
        log.info(String.valueOf(shareId));
        // 重置分享的 过期时间 访问限制
        fileShareService.adminResetShareExpire(shareId, expireType, accessLimit);
        log.info("重置分享成功");
        return Result.success();
    }

    /**
     * 启用禁用分享
     *
     * @return
     */
    @PostMapping("/ban")
    public Result banShare(@RequestBody ShareBanStatusDTO shareBanStatusDTO) {
        log.info("启用禁用成功");
        fileShareService.updateShareBanStatus(shareBanStatusDTO);
        return Result.success();
    }
}
