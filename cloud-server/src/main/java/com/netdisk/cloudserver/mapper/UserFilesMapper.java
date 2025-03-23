package com.netdisk.cloudserver.mapper;

import com.netdisk.entity.UserFiles;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserFilesMapper {
    /**
     * 根据 userId itemId 查询条目下的条目
     *
     * @param itemPId
     * @return
     */
    List<UserFiles> selectUserItemsByItemPId(Integer userId, Integer itemPId);

    /**
     * 根据 item_id 检查用户条目
     *
     * @param userId
     * @param itemId
     * @return
     */
    UserFiles selectUserItemByItemId(Integer userId, Integer itemId);

    /**
     * 根据item_id删除条目
     *
     * @param userId
     * @param itemId
     * @return
     */
    Integer deleteUserItemByItemId(Integer userId, Integer itemId);

    /**
     * 删除item_id这个条目的子条目
     *
     * @param userId
     * @param itemPId
     * @return
     */
    Integer deleteUserItemsByPId(Integer userId, Integer itemPId);

    /**
     * 新增用户条目
     *
     * @param userNewFolder
     */
    // TODO 多个Service Mapper中可能有重复的
    void insertNewItem(UserFiles userNewFolder);


    /**
     * 从 user_file 表中查询所有数据
     *
     * @return
     */
    List<UserFiles> selectAllUserItems();
}
