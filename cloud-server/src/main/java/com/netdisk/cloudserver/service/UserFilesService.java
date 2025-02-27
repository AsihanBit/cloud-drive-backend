package com.netdisk.cloudserver.service;

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
}
