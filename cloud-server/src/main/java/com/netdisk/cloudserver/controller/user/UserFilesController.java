package com.netdisk.cloudserver.controller.user;

import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.dto.CreateFolderDTO;
import com.netdisk.result.Result;
import com.netdisk.vo.UserItemsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/files")
@Slf4j
public class UserFilesController {

    private UserFilesService userFilesService;

    public UserFilesController(UserFilesService userFilesService) {
        this.userFilesService = userFilesService;
    }

    /**
     * 用户查看: 某一目录下的条目
     *
     * @param itemPId
     * @return
     */
    @GetMapping("/items")
    public Result<List<UserItemsVO>> getUserItems(@RequestParam Integer itemPId) {
        List<UserItemsVO> userItemsVOList = userFilesService.getUserItemsByPId(itemPId);
        return Result.success(userItemsVOList);
    }

    @PostMapping("/createFolder")
    public Result createNewFolder(@RequestBody CreateFolderDTO createFolderDTO) {
        log.info("创建新文件夹: pId={}, folderName={}", createFolderDTO.getPId(), createFolderDTO.getFolderName());
        // TODO 格式长度重复限制等
        // 用户创建新文件夹
        userFilesService.createNewFolder(createFolderDTO);
        return Result.success();
    }

    /**
     * 用户根据 item_id 删除条目
     *
     * @param itemId
     * @return
     */
    @DeleteMapping("/item")
    public Result deleteUserItems(@RequestParam Integer itemId) {
        // 0不存在 1删除成功?
        userFilesService.deleteUserItemByItemId(itemId);
        return Result.success();
    }
}
