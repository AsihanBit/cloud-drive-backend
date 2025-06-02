package com.netdisk.cloudserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.netdisk.cloudserver.mapper.FileMapper;
import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.cloudserver.mapper.UserMapper;
import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.StatusConstant;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.CreateFolderDTO;
import com.netdisk.dto.UserDTO;
import com.netdisk.dto.UserFileStatusDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.User;
import com.netdisk.entity.UserFiles;
import com.netdisk.exception.BaseException;
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
    private FileMapper fileMapper;
    private UserFilesMapper userFilesMapper;
    private UserMapper userMapper;
    private ElasticSearchUtils elasticSearchUtils;


    public UserFilesServiceImpl(
            FileMapper fileMapper,
            UserFilesMapper userFilesMapper,
            UserMapper userMapper,
            ElasticSearchUtils elasticSearchUtils) {
        this.fileMapper = fileMapper;
        this.userFilesMapper = userFilesMapper;
        this.userMapper = userMapper;
        this.elasticSearchUtils = elasticSearchUtils;
    }

    /**
     * 用户查看: 某一目录下的条目
     *
     * @param itemPId
     * @return
     */
    @Override
    public List<UserItemsVO> getUserItemsByPId(Integer itemPId) {
        // 获取用户id
        Integer uid = BaseContext.getCurrentId();
        // 根据用户id查询 item_id下的所有条目
        List<UserFiles> userFiles = userFilesMapper.selectUserItemsByItemPIdWithUserId(uid, itemPId);

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
        UserFiles userFile = userFilesMapper.selectUserItemByItemIdWithUserId(userId, itemId);
        if (userFile == null) {
            log.info("用户删除的文件不存在");
            // 自定义异常
        }
        // TODO 递归删除更深层级条目
        if (userFile.getItemType() == 0) {
            deleteUserItemByItemIdRecursively(userId, userFile);
        } else if (userFile.getItemType() == 1) {
            // DB 里删除
            userFilesMapper.deleteUserItemByItemId(userFile.getItemId());
            // 更新已用空间
            updateUsedSpace(userId, -userFile.getFileSize());
            // 同步至 ElasticSearch
            deleteInElasticSearch(userFile.getItemId());
            // 减少物理文件引用次数
            reduceReferenceCount(userFile);
        }
        // Mysql
        // 根据item_id删除条目, 删除操作所影响的行数
//        Integer delResultItemId = userFilesMapper.deleteUserItemByItemIdWithUserId(userId, itemId);
//        log.info("删除条目-影响行数:{}", delResultItemId);
        // 删除item_id这个条目的子条目
//        Integer delResultPId = userFilesMapper.deleteUserItemsByPId(userId, itemId);
//        log.info("删除子条目-影响行数:{}", delResultPId);

        // ElasticSearch 删除条目以及子条目
//        boolean esIsDeleted;
//        try {
//            esIsDeleted = elasticSearchUtils.deleteItemAndChildren(itemId);
//        } catch (Exception e) {
//            log.error("ElasticSearch 删除失败（不影响主流程）: {}", e.getMessage());
//        }

    }

    // ElasticSearch 删除条目
    private boolean deleteInElasticSearch(Integer itemId) {
        // 同步至 ElasticSearch
        boolean esIsDeleted;
        try {
            esIsDeleted = elasticSearchUtils.deleteItemByItemId(itemId);
        } catch (Exception e) {
            log.error("ElasticSearch 删除失败（不影响主流程）: {}", e.getMessage());
            // 这里决定失败时的返回值
            esIsDeleted = false;
        }
        return esIsDeleted;
    }


    private void deleteUserItemByItemIdRecursively(Integer userId, UserFiles userFiles) {
        Integer itemId = userFiles.getItemId();

        // 先获取所有子项
        List<UserFiles> userFilesSubList = userFilesMapper.selectUserItemsByItemPId(itemId);

        // 递归处理所有子项
        for (UserFiles subFile : userFilesSubList) {
            if (subFile.getItemType() == 0) { // 如果是文件夹，递归删除
                deleteUserItemByItemIdRecursively(userId, subFile);
            } else { // 如果是文件，直接删除
                // DB 内删除
                userFilesMapper.deleteUserItemByItemId(subFile.getItemId());
                // 更新用户已用空间
                updateUsedSpace(userId, -subFile.getFileSize());
                // 同步至 ElasticSearch
                deleteInElasticSearch(subFile.getItemId());
                // 减少物理文件引用次数
                reduceReferenceCount(subFile);
            }
        }

        // 最后删除当前文件夹
        userFilesMapper.deleteUserItemByItemId(itemId);
        deleteInElasticSearch(itemId);
    }

    /**
     * 减少文件引用次数
     */
    private void reduceReferenceCount(UserFiles userFile) {
        // 减少文件引用次数
        File file = fileMapper.selectFileByFileId(userFile.getFileId());
        Integer referenceCount = file.getReferenceCount() != null ? file.getReferenceCount() : 0;
        referenceCount -= 1;

        if (referenceCount <= 0) {
            referenceCount = 0;
        }
        fileMapper.updateReferenceCount(file.getFileId(), referenceCount);
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
        UserFiles userFolderItem = userFilesMapper.selectUserItemByItemIdWithUserId(userId, createFolderDTO.getPId());

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
        boolean isSuccess;
        try {
            isSuccess = elasticSearchUtils.insertUserFilesDoc(userNewFolder);
        } catch (Exception e) {
            log.error("ElasticSearch 保存失败（不影响主流程）: {}", e.getMessage());
        }
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
        UserFiles item = userFilesMapper.selectUserItemByItemIdWithUserId(userId, itemId);
        return item;
    }

    /**
     * 查询一个用户所有条目
     *
     * @param userId
     * @return
     */
    @Override
    public List<UserItemsVO> getUserItemsByUserId(Integer userId) {
        List<UserFiles> list = userFilesMapper.selectUserItemsByUserId(userId);
        List<UserItemsVO> userItemsVOS = BeanUtil.copyToList(list, UserItemsVO.class);
        return userItemsVOS;
    }

    /**
     * 查询所有用户条目
     *
     * @return
     */
    @Override
    public List<UserItemsVO> getUserItems() {
        List<UserFiles> userFiles = userFilesMapper.selectAllUserItems();
        List<UserItemsVO> userItemsVOS = BeanUtil.copyToList(userFiles, UserItemsVO.class);
        return userItemsVOS;
    }

    /**
     * 启用禁用用户条目
     *
     * @param userFileStatusDTO
     */
    @Override
    public void updateBanStatus(UserFileStatusDTO userFileStatusDTO) {
        Short banStatus = userFileStatusDTO.getBanStatus();

        if (banStatus == StatusConstant.ITEM_STATUS_FROZEN) {
            banStatus = StatusConstant.ITEM_STATUS_NORMAL;
        } else if (banStatus == StatusConstant.ITEM_STATUS_NORMAL) {
            banStatus = StatusConstant.ITEM_STATUS_FROZEN;
        }

        userFilesMapper.updateBanStatus(userFileStatusDTO.getItemId(), banStatus);

    }

    /**
     * 管理员根据 item_id 删除条目
     *
     * @param itemId
     */
    @Override
    public void adminDeleteUserFileByItemId(Integer itemId) {
        userFilesMapper.adminDeleteUserFileByItemId(itemId);
    }

    /**
     * 更新用户已用空间
     *
     * @param userId         用户ID
     * @param fileSizeChange 文件大小变化量(上传为正值,删除为负值)
     * @return 更新后的已用空间
     */
    @Override
    public void updateUsedSpace(Integer userId, Long fileSizeChange) {
        // 参数校验
        if (userId == null || fileSizeChange == null) {
            throw new IllegalArgumentException("用户ID和文件大小变化量不能为空");
        }

        // 获取用户当前已用空间
        User user = userMapper.selectUserByUserId(userId);

        if (user == null) {
            throw new BaseException("用户不存在");
        }

        Long currentUsedSpace = user.getUsedSpace() != null ? user.getUsedSpace() : 0L;
        Long newUsedSpace = currentUsedSpace + fileSizeChange;

        // 防止已用空间小于0
        if (newUsedSpace < 0) {
            log.info("用户 {} 的已用空间计算结果小于0,将设置为0", userId);
            newUsedSpace = 0L;
        }

        // 检查是否超出用户总空间配额
        Long totalSpace = user.getTotalSpace();
        if (fileSizeChange > 0 && totalSpace != null && newUsedSpace > totalSpace) {
            // 存储空间不足
            // TODO 文件上传前判断
        }

        // 更新已用空间
        user.setUsedSpace(newUsedSpace);
        UserDTO userDTO = UserDTO.builder()
                .userId(userId)
                .usedSpace(newUsedSpace)
                .build();
        userMapper.updateUser(userDTO);
        log.info("用户{}的已用空间已更新: {} -> {}", userId, currentUsedSpace, newUsedSpace);
    }


}
