package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.CreateFolderDTO;
import com.netdisk.entity.UserFiles;
import com.netdisk.utils.ElasticSearchUtils;
import com.netdisk.vo.UserItemsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserFilesServiceImpl implements UserFilesService {

    private static final Logger log = LoggerFactory.getLogger(UserFilesServiceImpl.class);
    private UserFilesMapper userFilesMapper;
    private ElasticSearchUtils elasticSearchUtils;


    public UserFilesServiceImpl(UserFilesMapper userFilesMapper, ElasticSearchUtils elasticSearchUtils) {
        this.userFilesMapper = userFilesMapper;
        this.elasticSearchUtils = elasticSearchUtils;
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
                    .pId(userFile.getPId())
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

    /**
     * 用户根据 item_id 删除条目
     *
     * @param itemId
     */
    @Override
    public void deleteUserItemByItemId(Integer itemId) {
        Integer userId = BaseContext.getCurrentId();
        // 检查用户文件是否存在
        UserFiles userFiles = userFilesMapper.selectUserItemByItemId(userId, itemId);
        if (userFiles == null) {
            log.info("用户删除的文件不存在");
            // 自定义异常
        }
        // TODO 递归删除更深层级条目
        // Mysql
        // 根据item_id删除条目, 删除操作所影响的行数
        Integer delResultItemId = userFilesMapper.deleteUserItemByItemId(userId, itemId);
        log.info("删除条目-影响行数:{}", delResultItemId);
        // 删除item_id这个条目的子条目
        Integer delResultPId = userFilesMapper.deleteUserItemsByPId(userId, itemId);
        log.info("删除子条目-影响行数:{}", delResultPId);

        // ElasticSearch 删除条目以及子条目
        boolean esIsDeleted = elasticSearchUtils.deleteItemAndChildren(itemId);

    }

    /**
     * 用户创建新文件夹
     *
     * @param createFolderDTO
     */
    @Override
    public void createNewFolder(CreateFolderDTO createFolderDTO) {
        Integer userId = BaseContext.getCurrentId();
        // 查询父条目信息
        UserFiles userFolderItem = userFilesMapper.selectUserItemByItemId(userId, createFolderDTO.getPId());

//        Short folderDirectoryLevel;
        short folderDirectoryLevel;
        if (createFolderDTO.getPId() == 0 && userFolderItem == null) {
//            folderDirectoryLevel = Short.valueOf("0");
//            folderDirectoryLevel = Short.parseShort("0");
            folderDirectoryLevel = 0;
        } else {
            folderDirectoryLevel = (short) (userFolderItem.getDirectoryLevel() + 1);
        }

        UserFiles userNewFolder = UserFiles.builder()
                .userId(userId)
                .itemName(createFolderDTO.getFolderName())
                .itemType(Short.valueOf("0"))
                .pId(createFolderDTO.getPId())
                .directoryLevel(folderDirectoryLevel)
                .upLoadTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .recycleStatus(Short.valueOf("0"))
                .build();
        userFilesMapper.insertNewItem(userNewFolder);

        // 保存至 ElasticSearch
        boolean isSuccess = elasticSearchUtils.insertUserFilesDoc(userNewFolder);
    }

    /**
     * 检查用户是否拥有条目
     *
     * @param itemId
     * @return
     */
    @Override
    public UserFiles checkItemOwnership(Integer itemId) {
        Integer userId = BaseContext.getCurrentId();
        UserFiles item = userFilesMapper.selectUserItemByItemId(userId, itemId);
        return item;
    }
}
