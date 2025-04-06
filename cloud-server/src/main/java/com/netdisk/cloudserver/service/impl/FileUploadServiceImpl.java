package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.FileUploadMapper;
import com.netdisk.cloudserver.service.FileUploadService;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.ChunkUploadDTO;
import com.netdisk.dto.FileExistenceCheckDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.MergeFileResult;
import com.netdisk.entity.UserFiles;
import com.netdisk.utils.ElasticSearchUtils;
import com.netdisk.utils.FileChunkUtil;
import com.netdisk.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    //    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);
    private FileUploadMapper fileUploadMapper;
    private FileChunkUtil fileChunkUtil;
    private RedisUtil redisUtil;
    private ElasticSearchUtils elasticSearchUtils;

    public FileUploadServiceImpl(
            FileUploadMapper fileUploadMapper,
            FileChunkUtil fileChunkUtil,
            RedisUtil redisUtil,
            ElasticSearchUtils elasticSearchUtils) {
        this.fileUploadMapper = fileUploadMapper;
        this.fileChunkUtil = fileChunkUtil;
        this.redisUtil = redisUtil;
        this.elasticSearchUtils = elasticSearchUtils;
    }

    /**
     * 秒传: 用户文件上传记录保存至 数据库
     *
     * @param fileExistenceCheckDTO
     */
    @Override
    public void userUploadFile(FileExistenceCheckDTO fileExistenceCheckDTO, File file) {
        Integer userId = BaseContext.getCurrentId();
        // 获取已存在文件的信息,大小,id等
//        fileUploadMapper.queryFileByHash(fileExistenceCheckDTO.getFileHash());
//        Short itemType = (short) 1;
        /**
         * √ ItemId
         * √ userId
         * √ itemName
         * √ itemType
         * √ pId
         * √ directoryLevel
         * √ fileId
         * √ fileSize
         * √ fileCover
         * √ fileExtension
         * √ upLoadTime
         * √ updateTime
         * √ recycleStatus
         */
        Short pIdDirectoryLevel = getPIdDirectoryLevel(fileExistenceCheckDTO.getTargetPathId());
        UserFiles userFile = UserFiles.builder()
                .userId(userId)
                .itemName(fileExistenceCheckDTO.getFileName())
                .itemType(Short.valueOf("1"))
                .pId(fileExistenceCheckDTO.getTargetPathId())
                .directoryLevel((short) (pIdDirectoryLevel + 1))
                .fileId(file.getFileId())
                .fileSize(file.getFileSize())
                .fileCover(file.getFileCover())
                .upLoadTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .recycleStatus(Short.valueOf("0"))
                .build();
        // 设置用户的文件扩展名
        String fileExtension = userFile.generateFileExtension();
        userFile.setFileExtension(fileExtension);
        Integer affectedRow = fileUploadMapper.insertUserFile(userFile);
        log.info("受影响的行数:{}", affectedRow);
        log.info("用户新增条目itemId:{}", userFile.getItemId());

        // 2. 保存至 ElasticSearch
        boolean isSuccess;
        try {
            isSuccess = elasticSearchUtils.insertUserFilesDoc(userFile);
        } catch (Exception e) {
            log.error("ElasticSearch 保存失败（不影响主流程）: {}", e.getMessage());
        }
    }

    /**
     * 上传分片
     *
     * @param chunkUploadDTO
     */
    @Override
    public void uploadChunk(ChunkUploadDTO chunkUploadDTO) {
        // 获取用户id
        Integer userId = BaseContext.getCurrentId();

        // 存储至本地
        fileChunkUtil.storeChunk(
                chunkUploadDTO.getFile(),
                chunkUploadDTO.getFileHash(),
                chunkUploadDTO.getChunkNumber(),
                chunkUploadDTO.getChunkHash());

        // 分片信息保存至 redis
        redisUtil.recordChunkUpload(userId, chunkUploadDTO.getFileHash(), chunkUploadDTO.getChunkNumber());
        log.info("分片:{} 保存成功", chunkUploadDTO.getChunkNumber());

        // 判断有没有保存完毕
        Long storedChunkCount = redisUtil.getChunkCount(userId, chunkUploadDTO.getFileHash());
        if (storedChunkCount == null) {
            log.info("文件分片信息不存在抛异常");
        }
        Integer storedChunkCountInt = storedChunkCount.intValue();
        if (storedChunkCountInt.equals(chunkUploadDTO.getChunkCount())) {
            // 全部分片保存完成
            log.info("全部分片保存完成");
            // TODO 这里返回后还需验证一下,计算的md5和一开始判断文件存在的一致性
            MergeFileResult mergeFileResult = fileChunkUtil.mergeChunks(chunkUploadDTO.getFileHash());
            // 保存至数据库 表 file user_file
            /**
             * 自增 fileId
             * √ fileMd5
             *   storageLocation
             * √ fileSize
             *   fileCover
             * √ referenceCount
             * √ userId
             *   transcodeStatus
             * √ banStatus
             * √ createTime
             * √ updateTime
             */
            File newFile = File.builder()
                    .fileMd5(mergeFileResult.getFileHash())
                    .storageLocation("/")
                    .fileSize(mergeFileResult.getFileSize())
                    .fileCover("/")
                    .referenceCount(1)
                    .userId(userId)
                    .transcodeStatus(Short.valueOf("1"))
                    .banStatus(Short.valueOf("0"))
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();


            // file表中创建新文件信息 ps:是受影响的行数
            Integer fileId = fileUploadMapper.insertNewFile(newFile);
            // 获取父文件夹PId的层级DirectoryLevel
            Short pIdDirectoryLevel = getPIdDirectoryLevel(chunkUploadDTO.getTargetPathId());
//            Short PIdDirectoryLevel;
//            if (chunkUploadDTO.getTargetPathId() == 0) {
//                PIdDirectoryLevel = Short.valueOf("0");
//            } else {
//                PIdDirectoryLevel = fileUploadMapper.getPIdDirectoryLevel(chunkUploadDTO.getTargetPathId());
//            }
            // 为什么这里xml where pid = #{PId} 查询后,每个分片都会保存到file?

            /**
             * 自增 ItemId
             * √ userId
             * √ itemName
             * √ itemType
             * √ pId
             * √ directoryLevel
             * √ fileId
             * √ fileSize
             * √ fileCover
             * √ fileExtension
             * √ upLoadTime
             * √ updateTime
             * √ recycleStatus
             */
            UserFiles userNewFile = UserFiles.builder()
                    .userId(userId)
                    .itemName(chunkUploadDTO.getFileName())
                    .itemType(Short.valueOf("1"))
                    .pId(chunkUploadDTO.getTargetPathId())
                    .directoryLevel((short) (pIdDirectoryLevel + 1))
//                    .fileId(fileId) // 是受影响的行数
                    .fileId(newFile.getFileId())
                    .fileSize(mergeFileResult.getFileSize()) // 冗余字段
                    .fileCover("/") // 冗余字段
                    .upLoadTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .recycleStatus(Short.valueOf("0"))
                    .build();
            // 设置用户指定的扩展名
            String fileExtension = userNewFile.generateFileExtension();
            userNewFile.setFileExtension(fileExtension);

            // user_file表中创建用户和文件的关联信息
            fileUploadMapper.insertUserFile(userNewFile);

            // 2. 保存至 ElasticSearch
            boolean isSuccess;
            try {
                isSuccess = elasticSearchUtils.insertUserFilesDoc(userNewFile);
            } catch (Exception e) {
                log.error("ElasticSearch 保存失败（不影响主流程）: {}", e.getMessage());
            }

            // 清除分片redis
            redisUtil.deleteAllChunk(userId, chunkUploadDTO.getFileHash());

            // 清除存储的所有分片文件 (可以紧接着在保存file后执行) (也可以在mergeChunks完成)
//            fileChunkUtil.deleteAllChunks(chunkUploadDTO.getFileHash());
        }


    }

    /**
     * 获取父文件夹PId的层级DirectoryLevel
     *
     * @param targetPathId
     * @return
     */
    public Short getPIdDirectoryLevel(Integer targetPathId) {
        Short PIdDirectoryLevel;
        if (targetPathId == 0) {
            PIdDirectoryLevel = Short.valueOf("0");
        } else {
            PIdDirectoryLevel = fileUploadMapper.getPIdDirectoryLevel(targetPathId);
        }
        return PIdDirectoryLevel;
    }


}
