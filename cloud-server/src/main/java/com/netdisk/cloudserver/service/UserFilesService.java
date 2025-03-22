package com.netdisk.cloudserver.service;

import com.netdisk.dto.CreateFolderDTO;
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
    List<UserItemsVO> getUserItems(Integer itemPId);

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

}
