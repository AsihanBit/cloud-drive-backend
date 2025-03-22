package com.netdisk.cloudserver.service;

import com.netdisk.dto.UserSaveSelectedItemsDTO;
import com.netdisk.dto.UserSharedDTO;
import com.netdisk.dto.UserSharedItemsDTO;
import com.netdisk.vo.ShareItemVO;
import com.netdisk.vo.UserShareVO;

import java.util.List;

public interface FileShareService {
    /**
     * 用户分享一系列条目
     *
     * @param userShareItemsDTO
     */
    void userShareItems(UserSharedItemsDTO userShareItemsDTO);

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
    void saveSelectedItems(UserSaveSelectedItemsDTO userSaveSelectedItemsDTO);

    /**
     * 获取外部分享的文件列表
     *
     * @param shareId
     * @param pItemId
     * @return
     */
    List<ShareItemVO> getOtherShareFiles(Integer shareId, Integer pItemId);
}
