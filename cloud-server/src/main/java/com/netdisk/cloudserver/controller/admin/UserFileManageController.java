package com.netdisk.cloudserver.controller.admin;

import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.dto.UserFileDTO;
import com.netdisk.dto.UserFileStatusDTO;
import com.netdisk.result.Result;
import com.netdisk.vo.UserItemsVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/userfile")
@Slf4j
public class UserFileManageController {
    private UserFilesService userFilesService;

    public UserFileManageController(UserFilesService userFilesService) {
        this.userFilesService = userFilesService;
    }

    /**
     * 查询所有用户条目
     *
     * @return
     */
    @GetMapping("/getall")
    public Result<List<UserItemsVO>> getAllUserItems() {
        log.info("查询所有用户条目成功");
        List<UserItemsVO> list = userFilesService.getUserItems();
        return Result.success(list);
    }

    /**
     * 查询一个用户所有条目
     *
     * @return
     */
    @GetMapping("/get")
    public Result<List<UserItemsVO>> getUserItemsByUserId(@RequestParam Integer userId) {
        log.info("查询一个用户条目成功");
        List<UserItemsVO> userItems = userFilesService.getUserItemsByUserId(userId);
        return Result.success(userItems);
    }


    /**
     * 修改用户条目
     *
     * @return
     */
    @PostMapping("/modify")
    public Result<UserItemsVO> modifyUserFile(@RequestBody UserFileDTO userFileDTO) {
        log.info("修改用户条目成功");
        return Result.success();
    }

    /**
     * 删除用户条目
     *
     * @return
     */
    @DeleteMapping("/delete")
    public Result deleteUserFile(@RequestParam Integer itemId) {
        log.info("删除用户条目成功/delete");
        userFilesService.deleteUserItemByItemId(itemId);
        return Result.success();
    }

    /**
     * 管理员根据 item_id 删除条目
     *
     * @param itemId
     * @return
     */
    @DeleteMapping("/item")
    public Result deleteUserItems(@RequestParam Integer itemId) {
        // 0不存在 1删除成功?
        log.info("删除用户条目成功/item");
        userFilesService.adminDeleteUserFileByItemId(itemId);
        return Result.success();
    }

    /**
     * 启用禁用用户条目
     *
     * @return
     */
    @PostMapping("/ban")
    public Result ban(@RequestBody UserFileStatusDTO userFileStatusDTO) {
        log.info("启用禁用用户条目成功");
        userFilesService.updateBanStatus(userFileStatusDTO);
        return Result.success();
    }


}
