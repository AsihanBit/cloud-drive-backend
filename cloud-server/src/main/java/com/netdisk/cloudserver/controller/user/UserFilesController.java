package com.netdisk.cloudserver.controller.user;

import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.result.Result;
import com.netdisk.vo.UserItemsVO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/files")
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
        List<UserItemsVO> userItemsVOList = userFilesService.getUserItems(itemPId);
        return Result.success(userItemsVOList);
    }
}
