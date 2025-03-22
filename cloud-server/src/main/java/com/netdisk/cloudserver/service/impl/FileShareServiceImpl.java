package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.FileShareMapper;
import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.cloudserver.mapper.UserMapper;
import com.netdisk.cloudserver.service.FileShareService;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.UserSaveSelectedItemsDTO;
import com.netdisk.dto.UserSharedDTO;
import com.netdisk.dto.UserSharedItemsDTO;
import com.netdisk.entity.Share;
import com.netdisk.entity.ShareItem;
import com.netdisk.entity.User;
import com.netdisk.entity.UserFiles;
import com.netdisk.enums.ShareExpirationEnums;
import com.netdisk.utils.RedisUtil;
import com.netdisk.utils.ShareCodeUtil;
import com.netdisk.vo.ShareItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileShareServiceImpl implements FileShareService {
    private FileShareMapper fileShareMapper;
    private UserFilesMapper userFilesMapper;
    private UserMapper userMapper;
    private RedisUtil redisUtil;


    public FileShareServiceImpl(
            FileShareMapper fileShareMapper,
            UserFilesMapper userFilesMapper,
            UserMapper userMapper,
            RedisUtil redisUtil) {
        this.fileShareMapper = fileShareMapper;
        this.userFilesMapper = userFilesMapper;
        this.userMapper = userMapper;
        this.redisUtil = redisUtil;
    }

    /**
     * 用户分享一系列条目
     *
     * @param userShareItemsDTO
     */
    @Override
    public void userShareItems(UserSharedItemsDTO userShareItemsDTO) {
        Integer userId = BaseContext.getCurrentId();
        // 生成提取码
        String shareCode = ShareCodeUtil.generateShareCode();

        // 获取有效期类型
        ShareExpirationEnums expiration = ShareExpirationEnums.getExpirationByCode(userShareItemsDTO.getExpireType());
        // 计算到期时间
        LocalDateTime createTime = LocalDateTime.now(); // 当前时间作为创建时间
        LocalDateTime expireTime = expiration.calculateExpireTime(createTime);
        User user = userMapper.selectUserByUserId(userId);

        // 加入share表
        Share share = Share.builder()
                .shareCode(shareCode)
                .userId(userId)
                .username(user.getUsername())
                .nickname(user.getNickname())
                .expireType(userShareItemsDTO.getExpireType())
                .expireTime(expireTime)
                .accessCount(0)
                .accessLimit(userShareItemsDTO.getAccessLimit())
                .shareStatus(Short.valueOf("1"))
                .createTime(createTime)
                .build();
        // 新增分享: ps:返回的是受影响的行数,获取主键直接把上面 share.getXxx就可以
        Integer shareId = fileShareMapper.insertShare(share);
        // 遍历子文件加入share_item表
        for (Integer itemId : userShareItemsDTO.getItemIds()) {
            UserFiles userItem = userFilesMapper.selectUserItemByItemId(userId, itemId);
            ShareItem shareItem = ShareItem.builder()
//                    .shareId(shareId) // 这是受影响的行数
                    .shareId(share.getShareId())
                    .pShareItemId(0)
                    .userId(userId)
                    .itemId(itemId)
                    .itemName(userItem.getItemName())
                    .itemType(userItem.getItemType())
                    .fileId(userItem.getFileId())
                    .fileSize(userItem.getFileSize())
                    .fileCover(userItem.getFileCover())
                    .fileExtension(userItem.getFileExtension())
                    .updateTime(userItem.getUpdateTime())
                    .build();
            // 对于分享 添加用户分享的条目
            fileShareMapper.insertShareItem(shareItem);
            // 如果 userItem.getFileSize()==0 是文件夹,还需遍历递归文件夹
            // 根据userFilesMapper.selectUserItemsByItemPId获取每个item的子item, 然后里面的文件夹,再进行遍历

            // 如果当前条目是文件夹，递归处理其子文件和子文件夹
            if (userItem.getItemType() == 0) { // 假设 0 表示文件夹
                // 第一个参数不应该是 shareId
                addShareItemsRecursively(share.getShareId(), shareItem.getShareItemId(), userId, itemId);
            }

        }


    }

    private void addShareItemsRecursively(Integer shareId, Integer pShareItemId, Integer userId, Integer itemId) {
        // 获取当前文件夹的子文件和子文件夹
        List<UserFiles> childItems = userFilesMapper.selectUserItemsByItemPId(userId, itemId);

        for (UserFiles childItem : childItems) {
            // 创建 shareItem 对象
            ShareItem shareItem = ShareItem.builder()
                    .shareId(shareId)
                    .pShareItemId(pShareItemId) // 父文件夹的 share_item_id
                    .userId(userId)
                    .itemId(childItem.getItemId())
                    .itemName(childItem.getItemName())
                    .itemType(childItem.getItemType())
                    .fileId(childItem.getFileId())
                    .fileSize(childItem.getFileSize())
                    .fileCover(childItem.getFileCover())
                    .fileExtension(childItem.getFileExtension())
                    .updateTime(childItem.getUpdateTime())
                    .build();

            // 添加到 share_item 表
            fileShareMapper.insertShareItem(shareItem);

            // 如果当前条目是文件夹，递归处理其子文件和子文件夹
            if (childItem.getItemType() == 0) { // 假设 0 表示文件夹
                addShareItemsRecursively(shareId, shareItem.getShareItemId(), userId, childItem.getItemId());
            }
        }
    }

    /**
     * 获取用户自己分享过的文件列表
     *
     * @return
     */
    @Override
    public List<UserSharedDTO> getUserOwnSharedList() {
        Integer userId = BaseContext.getCurrentId();
        List<UserSharedDTO> userSharedItemList = fileShareMapper.getUserOwnSharedList(userId);
        return userSharedItemList;
    }

    /**
     * 批量删除选中的分享
     *
     * @param userSharedItemsDTO
     */
    @Override
    public void deleteSharedItems(UserSharedItemsDTO userSharedItemsDTO) {
        // 根据 shareId 数组删除分享
        List<Integer> shareIds = userSharedItemsDTO.getItemIds();
        for (Integer shareId : shareIds) {
            // 删除 share 表
            fileShareMapper.deleteSharedItemById(shareId);
            // 删除 share_item 表
            fileShareMapper.deleteSharedItemFilesById(shareId);
        }
    }

    /**
     * 根据 分享id 获取自己分享的文件列表
     *
     * @param shareId
     * @return
     */
    @Override
    public List<ShareItemVO> getMyShareFiles(Integer shareId, Integer pItemId) {
        Integer userId = BaseContext.getCurrentId();
        // TODO 每个类似业务都带上 userId (此业务已带)
        List<ShareItemVO> shareItems = fileShareMapper.getMyShareFilesByShareId(shareId, pItemId, userId);
        return shareItems;
    }

    /**
     * 获取外部分享的文件列表
     *
     * @param shareId
     * @param pItemId
     * @return
     */
    @Override
    public List<ShareItemVO> getOtherShareFiles(Integer shareId, Integer pItemId) {
        List<ShareItemVO> shareItems = fileShareMapper.getOtherShareFilesByShareId(shareId, pItemId);
        return shareItems;
    }

    /**
     * 重置分享的 过期时间 访问限制
     *
     * @param shareId
     * @param expireType
     * @param accessLimit
     */
    @Override
    public void resetShareExpire(Integer shareId, Short expireType, Integer accessLimit) {
        Integer userId = BaseContext.getCurrentId();

        ShareExpirationEnums expiration = ShareExpirationEnums.getExpirationByCode(expireType);
        // 计算到期时间
        LocalDateTime expireTime = expiration.calculateExpireTime(LocalDateTime.now());
        fileShareMapper.resetShareExpire(shareId, userId, expireType, expireTime, accessLimit);
    }

    /**
     * 使用分享链接 提取码提取文件列表
     *
     * @param shareId
     * @param extractCode
     * @return
     */
    @Override
    public List<ShareItemVO> useShareLink(Integer shareId, String extractCode) {
        Integer userId = BaseContext.getCurrentId();
        // 1 先使用 share_id 查询 Share
        Share share = fileShareMapper.getShareByShareId(shareId);
        // TODO 此处可以解密 加密后的分享码
        if (share == null) {
            System.out.println("不存在这个分享");
            return null;
        }
        if (!share.getShareCode().equals(extractCode)) {
            System.out.println("提取码错误");
            return null;
        }
        // 2 如果 userId 不等于分享者,+1访问次数,访问次数到限制不能访问
        if (!share.getUserId().equals(userId)) {
            Integer accessCount = share.getAccessCount() + 1;
            // 分享 - 增加一次访问记录
            fileShareMapper.incrementShareAccessCount(shareId, accessCount);
        }

        // 3 返回分享里的文件列表
        Integer pShareItemId = 0;
        List<ShareItemVO> shareItems = fileShareMapper.getShareFilesByShareIdAndPId(shareId, pShareItemId);

        return shareItems;
    }

    /**
     * 转存文件
     *
     * @param userSaveSelectedItemsDTO
     */
    @Transactional
    @Override
    public void saveSelectedItems(UserSaveSelectedItemsDTO userSaveSelectedItemsDTO) {
        // TODO 增加转存记录
        // 1 查询分享信息
        Share share = fileShareMapper.getShareByShareId(userSaveSelectedItemsDTO.getShareId());
        if (share == null) {
            System.out.println("不存在这个分享");
            return;
        }
        if (!share.getShareCode().equals(userSaveSelectedItemsDTO.getExtractCode())) {
            System.out.println("提取码错误");
            return;
        }
        // 直接禁止转存自己目录
        Integer userId = BaseContext.getCurrentId();
        if (share.getUserId().equals(userId)) {
            System.out.println("防止无限递归转存,禁止转存自己文件夹");
            return;
        }

        // 2 保存文件到用户目录
        LocalDateTime now = LocalDateTime.now();
        Integer pItemId = userSaveSelectedItemsDTO.getFolderId();
        List<Integer> selectedShareItemIds = userSaveSelectedItemsDTO.getSelectedItemIds();
        // 使用 share_item_id 数组获取分享的条目列表
        List<ShareItem> shareItems = fileShareMapper.getShareItemsByShareItemIds(selectedShareItemIds);

        // 递归转存文件夹及其子文件
        for (ShareItem shareItem : shareItems) {
            saveItemRecursively(userId, pItemId, now, share.getUserId(), shareItem.getItemId());
        }

//        for (ShareItem shareItem : shareItems) {
//            UserFiles userItem = userFilesMapper.selectUserItemByItemId(share.getUserId(), shareItem.getItemId());
//            if (userItem == null) {
//                // 分享的用户已删除源文件
//                return;
//            }
//            userItem.setUserId(userId);
//            userItem.setPId(pItemId);
//            userItem.setUpLoadTime(now);
//
//            Short itemType = userItem.getItemType();
//
//
//            if (itemType == 0) {
//                System.out.println("转存的是文件夹");
//            } else if (itemType == 1) {
//                System.out.println("转存的是文件");
////                System.out.println("转存文件的原主键ItemId" + userItem.getItemId());
//                // directory_level
//                userFilesMapper.insertNewItem(userItem);
//                // 此时已更新 新主键 item_id
//                System.out.println("转存文件的新主键ItemId" + userItem.getItemId());
//            }
//        }


    }

    /**
     * 递归转存文件夹及其子文件
     * 注: 这里转存目录中转存它的父文件夹,会无限递归,所以直接禁止转存自己的文件
     *
     * @param userId      当前用户 ID
     * @param pItemId     父文件夹 ID
     * @param now         当前时间
     * @param shareUserId 分享用户的 ID
     * @param itemId      当前条目 ID
     */
    private void saveItemRecursively(Integer userId, Integer pItemId, LocalDateTime now, Integer shareUserId, Integer itemId) {
        // 查询分享的条目
        UserFiles userItem = userFilesMapper.selectUserItemByItemId(shareUserId, itemId);
        if (userItem == null) {
            // 分享的用户已删除源文件
            return;
        }

        // 设置新条目的属性
        userItem.setUserId(userId);
        userItem.setPId(pItemId);
        userItem.setUpLoadTime(now);

        // 获取条目类型
        Short itemType = userItem.getItemType();

        if (itemType == 0) {
            // 转存的是文件夹
            System.out.println("转存的是文件夹");

            // 查询文件夹的子文件和子文件夹
            List<UserFiles> childItems = userFilesMapper.selectUserItemsByItemPId(shareUserId, itemId);


            // 保存文件夹
            userFilesMapper.insertNewItem(userItem);
            Integer newItemId = userItem.getItemId(); // 获取新生成的 itemId


            // 递归转存子文件和子文件夹
            for (UserFiles childItem : childItems) {
                saveItemRecursively(userId, newItemId, now, shareUserId, childItem.getItemId());
            }
        } else if (itemType == 1) {
            // 转存的是文件
            System.out.println("转存的是文件");

            // 保存文件
            userFilesMapper.insertNewItem(userItem);
            System.out.println("转存文件的新主键ItemId: " + userItem.getItemId());
        }
    }
}
