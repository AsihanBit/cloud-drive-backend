package com.netdisk.cloudserver.service;

import com.netdisk.dto.*;
import com.netdisk.entity.Share;
import com.netdisk.entity.ShareItem;
import com.netdisk.enums.ShareTransferEnum;
import com.netdisk.vo.ShareItemVO;
import com.netdisk.vo.ShareVO;

import java.util.List;

public interface FileShareService {
    /**
     * 用户分享一系列条目
     *
     * @param userShareItemsDTO
     */
    ShareResultDTO userShareItems(UserSharedItemsDTO userShareItemsDTO);

    /**
     * 获取用户自己分享过的文件列表
     *
     * @return
     */
    List<UserSharedDTO> getUserOwnSharedList();

    /**
     * 批量删除选中的分享
     *
     * @param userSharedItemsDTO
     */
    void deleteSharedItems(UserSharedItemsDTO userSharedItemsDTO);

    /**
     * 根据 分享id 获取自己分享的文件列表
     *
     * @param shareId
     * @return
     */
    List<ShareItemVO> getMyShareFiles(Integer shareId, Integer pItemId);

    /**
     * 重置分享的 过期时间 访问限制
     *
     * @param shareId
     * @param expireType
     * @param accessLimit
     */
    void resetShareExpire(Integer shareId, Short expireType, Integer accessLimit);

    /**
     * 使用分享链接 提取码提取文件列表
     *
     * @param shareId
     * @param extractCode
     * @return
     */
    List<ShareItemVO> useShareLink(Integer shareId, String extractCode);

    /**
     * 转存文件
     *
     * @param userSaveSelectedItemsDTO
     */
    ShareTransferEnum saveSelectedItems(UserSaveSelectedItemsDTO userSaveSelectedItemsDTO);

    /**
     * 获取外部分享的文件列表
     *
     * @param shareId
     * @param pItemId
     * @return
     */
    List<ShareItemVO> getOtherShareFiles(Integer shareId, Integer pItemId);

    /**
     * 查询所有分享
     *
     * @return
     */
    List<ShareVO> getAllShare();

    /**
     * id查询分享
     *
     * @return
     */
    Share getShareById(Integer shareId);

    /**
     * 根据用户id获取分享列表
     *
     * @param userId
     * @return
     */
    List<Share> getShareByUserId(Integer userId);

    /**
     * id查询分享的每个条目
     *
     * @param shareId
     * @return
     */
    List<ShareItem> getShareItemsByShareId(Integer shareId);

    /**
     * id删除分享
     *
     * @param shareId
     */
    void deleteShareByShareId(Integer shareId);

    /**
     * 重置分享的 过期时间 访问限制
     *
     * @param shareId
     * @param expireType
     * @param accessLimit
     */
    void adminResetShareExpire(Integer shareId, Short expireType, Integer accessLimit);

    /**
     * 启用禁用分享
     *
     * @param shareBanStatusDTO
     */
    void updateShareBanStatus(ShareBanStatusDTO shareBanStatusDTO);
}
