package com.netdisk.cloudserver.service;

import com.netdisk.dto.CreateFolderDTO;
import com.netdisk.dto.UserFileStatusDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.UserFiles;
import com.netdisk.vo.UserItemsVO;

import java.util.List;

public interface UserFilesService {
    /**
     * 用户查看: 某一目录下的条目
     *
     * @param itemPId
     * @return
     */
    List<UserItemsVO> getUserItemsByPId(Integer itemPId);

    /**
     * 用户根据 item_id 删除条目
     *
     * @param itemId
     */
    void deleteUserItemByItemId(Integer itemId);

    /**
     * 用户创建新文件夹
     *
     * @param createFolderDTO
     */
    void createNewFolder(CreateFolderDTO createFolderDTO);

    /**
     * 检查用户是否拥有条目
     *
     * @param itemId
     * @return
     */
    UserFiles checkItemOwnership(Integer itemId);

    /**
     * 查询一个用户所有条目
     *
     * @param userId
     * @return
     */
    List<UserItemsVO> getUserItemsByUserId(Integer userId);

    /**
     * 查询所有用户条目
     *
     * @return
     */
    List<UserItemsVO> getUserItems();

    /**
     * 启用禁用用户条目
     *
     * @param userFileStatusDTO
     */
    void updateBanStatus(UserFileStatusDTO userFileStatusDTO);

    /**
     * 管理员根据 item_id 删除条目
     *
     * @param itemId
     */
    void adminDeleteUserFileByItemId(Integer itemId);

    /**
     * 更新用户已用空间
     *
     * @param userId
     * @param fileSizeChange
     */
    void updateUsedSpace(Integer userId, Long fileSizeChange);


}
