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
}
