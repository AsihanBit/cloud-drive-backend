package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.context.BaseContext;
import com.netdisk.entity.UserFiles;
import com.netdisk.vo.UserItemsVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserFilesServiceImpl implements UserFilesService {

    private UserFilesMapper userFilesMapper;

    public UserFilesServiceImpl(UserFilesMapper userFilesMapper) {
        this.userFilesMapper = userFilesMapper;
    }

    /**
     * 用户查看: 某一目录下的条目
     *
     * @param itemPId
     * @return
     */
    @Override
    public List<UserItemsVO> getUserItems(Integer itemPId) {
        // 获取用户id
        Integer uid = BaseContext.getCurrentId();
        // 根据用户id查询 item_id下的所有条目
        List<UserFiles> userFiles = userFilesMapper.selectUserItemsByItemPId(uid, itemPId);

        List<UserItemsVO> userItemsVOList = new ArrayList<>();
        for (UserFiles userFile : userFiles) {
            UserItemsVO userItemsVO = UserItemsVO.builder()
                    .itemId(userFile.getItemId())
                    .itemName(userFile.getItemName())
                    .itemType(userFile.getItemType())
                    .directoryLevel(userFile.getDirectoryLevel())
                    .fileId(userFile.getFileId())
                    .fileSize(userFile.getFileSize())
                    .fileExtension(userFile.getFileExtension())
                    .updateTime(userFile.getUpdateTime())
                    .build();
            userItemsVOList.add(userItemsVO);
        }

        return userItemsVOList;
    }
}
