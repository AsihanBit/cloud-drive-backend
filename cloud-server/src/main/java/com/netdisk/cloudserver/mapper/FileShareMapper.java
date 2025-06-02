package com.netdisk.cloudserver.mapper;

import com.netdisk.dto.ShareBanStatusDTO;
import com.netdisk.dto.SharedDTO;
import com.netdisk.entity.Share;
import com.netdisk.entity.ShareItem;
import com.netdisk.vo.ShareItemVO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FileShareMapper {
    /**
     * 新增分享
     *
     * @param share
     */
    Integer insertShare(Share share);

    /**
     * 对于分享 添加用户分享的条目
     * 使用了 useGeneratedKeys ,没必要返回 Integer,返回值是受影响的行数. 直接shareItem.getXxx就可获取主键值
     *
     * @param shareItem
     */
    void insertShareItem(ShareItem shareItem);

    /**
     * 获取用户自己分享过的文件列表
     *
     * @param userId
     * @return
     */
    List<SharedDTO> getUserOwnSharedList(Integer userId);


    /**
     * 根据 shareId 删除 share 表
     *
     * @param shareId
     */
    // TODO 下面两个方法待加 userid
    void deleteSharedItemById(Integer shareId);

    /**
     * 根据 shareId 删除 share_item 表
     *
     * @param shareId
     */
    void deleteSharedItemFilesById(Integer shareId);

    /**
     * 根据 分享id 获取自己分享的文件列表
     *
     * @param shareId
     * @param userId
     * @return
     */
    List<ShareItemVO> getMyShareFilesByShareId(Integer shareId, Integer pItemId, Integer userId);

    /**
     * 获取外部分享的文件列表
     *
     * @param shareId
     * @param pItemId
     * @return
     */
    List<ShareItemVO> getOtherShareFilesByShareId(Integer shareId, Integer pItemId);


    /**
     * 重置分享的 过期时间 访问限制
     *
     * @param shareId
     * @param userId
     * @param expireType
     * @param expireTime
     * @param accessLimit
     */
    void resetShareExpire(Integer shareId, Integer userId, Short expireType, LocalDateTime expireTime, Integer accessLimit);

    /**
     * 使用 share_code share_id 查询 Share
     *
     * @param shareId
     * @return
     */
    Share getShareByShareId(Integer shareId);


    /**
     * 分享 - 增加一次访问记录
     *
     * @param shareId
     * @param accessCount
     */
    void incrementShareAccessCount(Integer shareId, Integer accessCount);

    /**
     * 返回分享里的文件列表
     *
     * @param shareId
     * @param pShareItemId
     * @return
     */
    List<ShareItemVO> getShareFilesByShareIdAndPId(Integer shareId, Integer pShareItemId);

    /**
     * 使用 share_item_id 数组获取分享的条目列表
     *
     * @param selectedShareItemIds
     * @return
     */
    List<ShareItem> getShareItemsByShareItemIds(List<Integer> selectedShareItemIds);


    /**
     * 查询所有分享
     *
     * @return
     */
    List<Share> selectAllShare();

    /**
     * id查询分享
     *
     * @return
     */
    Share selectShareById(Integer shareId);

    /**
     * 用户id搜索分享
     *
     * @param userId
     * @return
     */
    List<Share> selectShareByUserId(Integer userId);

    /**
     * id查询分享的每个条目
     *
     * @param shareId
     * @return
     */
    List<ShareItem> getShareItemsByShareId(Integer shareId);

    /**
     * 管理员 重置分享的 过期时间 访问限制
     *
     * @param shareId
     * @param expireType
     * @param expireTime
     * @param accessLimit
     */
    void adminResetShareExpire(Integer shareId, Short expireType, LocalDateTime expireTime, Integer accessLimit);

    /**
     * 启用禁用分享
     *
     * @param shareBanStatusDTO
     */
    void updateShareBanStatus(ShareBanStatusDTO shareBanStatusDTO);
}
