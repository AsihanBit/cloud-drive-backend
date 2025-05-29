package com.netdisk.cloudserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.netdisk.cloudserver.mapper.FileMapper;
import com.netdisk.cloudserver.service.FileService;
import com.netdisk.constant.StatusConstant;
import com.netdisk.dto.FileBanStatusDTO;
import com.netdisk.entity.File;
import com.netdisk.vo.FileVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileServiceImpl implements FileService {
    private FileMapper fileMapper;

    public FileServiceImpl(FileMapper fileMapper) {
        this.fileMapper = fileMapper;
    }

    /**
     * 表 file 中根据 file_id 查询文件
     *
     * @param fileId
     * @return
     */
    @Override
    public File queryFileByFileId(Integer fileId) {
        File file = fileMapper.selectFileByFileId(fileId);
        return file;
    }

    /**
     * 查询所有文件
     *
     * @return
     */
    @Override
    public List<FileVO> getAllFiles() {
        List<File> fileList = fileMapper.selectAllFiles();
        List<FileVO> fileVOList = BeanUtil.copyToList(fileList, FileVO.class);
        return fileVOList;
    }

    /**
     * id删除文件
     *
     * @param fileId
     */
    @Override
    public void deleteFileByFileId(Integer fileId) {
        fileMapper.deleteFileByFileId(fileId);
    }

    /**
     * 启用禁用文件
     *
     * @param fileBanStatusDTO
     */
    @Override
    public void updateFileBanStatus(FileBanStatusDTO fileBanStatusDTO) {
        if (fileBanStatusDTO.getBanStatus() == StatusConstant.ITEM_STATUS_NORMAL) {
            fileBanStatusDTO.setBanStatus(StatusConstant.ITEM_STATUS_LOCKED);
        } else if (fileBanStatusDTO.getBanStatus() == StatusConstant.ITEM_STATUS_LOCKED) {
            fileBanStatusDTO.setBanStatus(StatusConstant.ITEM_STATUS_NORMAL);
        }
        fileMapper.updateFileBanStatus(fileBanStatusDTO);
    }
}
