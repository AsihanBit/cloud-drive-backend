package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.FileMapper;
import com.netdisk.cloudserver.mapper.FileUploadMapper;
import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.cloudserver.mapper.UserMapper;
import com.netdisk.cloudserver.service.FileUploadService;
import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.StatusConstant;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.ChunkUploadDTO;
import com.netdisk.dto.FileExistenceCheckDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.MergeFileResult;
import com.netdisk.entity.User;
import com.netdisk.entity.UserFiles;
import com.netdisk.exception.BaseException;
import com.netdisk.utils.ElasticSearchUtils;
import com.netdisk.utils.FileChunkUtil;
import com.netdisk.utils.FileUtils;
import com.netdisk.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    //    private static final Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);
    private UserService userService;
    private UserMapper userMapper;
    private UserFilesService userFilesService;
    private FileMapper fileMapper;
    private FileUploadMapper fileUploadMapper;
    private FileChunkUtil fileChunkUtil;
    private RedisUtil redisUtil;
    private ElasticSearchUtils elasticSearchUtils;
    private ExecutorService executorService;
    private RedissonClient redissonClient;

    public FileUploadServiceImpl(
            UserService userService,
            UserMapper userMapper,
            UserFilesService userFilesService,
            FileMapper fileMapper,
            FileUploadMapper fileUploadMapper,
            FileChunkUtil fileChunkUtil,
            RedisUtil redisUtil,
            ElasticSearchUtils elasticSearchUtils,
            ExecutorService executorService,
            RedissonClient redissonClient) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.userFilesService = userFilesService;
        this.fileMapper = fileMapper;
        this.fileUploadMapper = fileUploadMapper;
        this.fileChunkUtil = fileChunkUtil;
        this.redisUtil = redisUtil;
        this.elasticSearchUtils = elasticSearchUtils;
        this.executorService = executorService;
        this.redissonClient = redissonClient;
    }

    /**
     * 秒传: 用户文件上传记录保存至 数据库
     *
     * @param fileExistenceCheckDTO
     */
    @Override
    public void userUploadFileExist(FileExistenceCheckDTO fileExistenceCheckDTO, File file) {
        // 获取已存在文件的信息,大小,id等
//        fileUploadMapper.queryFileByHash(fileExistenceCheckDTO.getFileHash());
//        Short itemType = (short) 1;
        // 1. 文件添加到用户账户
        Integer userId = BaseContext.getCurrentId();
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
        UserFiles newUserFile = UserFiles.builder()
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
                .recycleStatus(Short.valueOf("1"))
                .banStatus(Short.valueOf("1"))
                .build();
        // 设置用户的文件扩展名
        String fileExtension = newUserFile.generateFileExtension();
        newUserFile.setFileExtension(fileExtension);

        // user_file表中创建用户和文件的关联信息
        userUploadFile(newUserFile);
    }

    private void userUploadFile(UserFiles newUserFile) {

        // 1. user_file表中创建用户和文件的关联信息
        Integer affectedRow = fileUploadMapper.insertUserFile(newUserFile);
        // 更新用户已用空间 增加 所以是正号
        userFilesService.updateUsedSpace(newUserFile.getUserId(), newUserFile.getFileSize());

        // 2. 增加文件引用次数
        File file = fileMapper.selectFileByFileId(newUserFile.getFileId());
        Integer referenceCount = file.getReferenceCount() != null ? file.getReferenceCount() : 0;
        referenceCount += 1;
        fileMapper.updateReferenceCount(file.getFileId(), referenceCount);

        // 3. 保存至 ElasticSearch
        boolean isSuccess;
        try {
            isSuccess = elasticSearchUtils.insertUserFilesDoc(newUserFile);
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

        // 1. 分片存储至本地
        fileChunkUtil.storeChunk(
                chunkUploadDTO.getFile(),
                chunkUploadDTO.getFileHash(),
                chunkUploadDTO.getChunkNumber(),
                chunkUploadDTO.getChunkHash());

        // 2. 分片信息保存至 redis
//        redisUtil.recordChunkUpload(userId, chunkUploadDTO.getFileHash(), chunkUploadDTO.getChunkNumber());
        fileChunkUtil.recordChunkUpload(userId, chunkUploadDTO.getFileHash(), chunkUploadDTO.getChunkNumber());
        log.info("分片:{} 保存成功", chunkUploadDTO.getChunkNumber());

        // 3. 判断是否所有分片已保存完毕
        Long storedChunkCount = fileChunkUtil.getChunkCount(userId, chunkUploadDTO.getFileHash());
        if (storedChunkCount == null) {
            log.warn("文件分片信息不存在，可能 Redis 记录失败，抛异常");
            return;
        }
        // 全部分片保存完成
        // TODO 这里返回后还需验证一下,计算的md5和一开始判断文件存在的一致性


        // 4. 处理所有分片: 合并,写入DB,ES
        // (storedChunkCount.intValue() == chunkUploadDTO.getChunkCount())
        Integer storedChunkCountInt = storedChunkCount.intValue();
        if (storedChunkCountInt.equals(chunkUploadDTO.getChunkCount())) {
            // 尝试加锁，避免重复触发合并
            String lockKey = "merge:lock:" + chunkUploadDTO.getFileHash();
            RLock lock = redissonClient.getLock(lockKey);
            boolean isLocked = false;

            try {
                // 0 秒等待获取锁，锁定 3 分钟后自动释放
                isLocked = lock.tryLock(0, 3, TimeUnit.MINUTES);
                if (isLocked) {
                    log.info("触发文件合并任务：{}", chunkUploadDTO.getFileHash());
                    boolean finalIsLocked = isLocked;
                    executorService.submit(() -> {
                        try {
                            {
                                // 合并文件
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
                                        .referenceCount(0)
                                        .userId(userId)
                                        .userExtension(FileUtils.getFileExtension(chunkUploadDTO.getFileName()))
                                        .transcodeStatus(Short.valueOf("1"))
                                        .banStatus(StatusConstant.ITEM_STATUS_NORMAL)
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
                                UserFiles newUserFile = UserFiles.builder()
                                        .userId(userId)
                                        .itemName(chunkUploadDTO.getFileName())
                                        .itemType(Short.valueOf("1"))
                                        .pId(chunkUploadDTO.getTargetPathId())
                                        .directoryLevel((short) (pIdDirectoryLevel + 1))
                                        // .fileId(fileId)   // 是受影响的行数
                                        .fileId(newFile.getFileId())
                                        .fileSize(mergeFileResult.getFileSize()) // 冗余字段
                                        .fileCover("/") // 冗余字段
                                        .upLoadTime(LocalDateTime.now())
                                        .updateTime(LocalDateTime.now())
                                        .recycleStatus(Short.valueOf("0"))
                                        .build();
                                // 设置用户指定的扩展名
                                String fileExtension = newUserFile.generateFileExtension();
                                newUserFile.setFileExtension(fileExtension);

                                // user_file表中创建用户和文件的关联信息
                                userUploadFile(newUserFile);

                                // 清除分片redis
                                fileChunkUtil.deleteAllChunk(userId, chunkUploadDTO.getFileHash());
                                log.info("文件合并完成并清理缓存: {}", chunkUploadDTO.getFileHash());
                                // 清除存储的所有分片文件 (可以紧接着在保存file后执行) (也可以在mergeChunks完成)
                                fileChunkUtil.deleteAllChunks(chunkUploadDTO.getFileHash());

                            }
                        } catch (Exception e) {
                            log.error("合并任务执行失败: {}", e.getMessage(), e);
                        } finally {
                            // Redisson 安全释放锁 TODO 待优化
                            if (finalIsLocked && lock.isHeldByCurrentThread()) {
                                lock.unlock();
                                log.info("Redisson已释放锁");
                            }
                        }

                    });

                }

            } catch (InterruptedException e) {
                log.error("获取 Redisson 分布式锁失败: {}", e.getMessage(), e);
                Thread.currentThread().interrupt();
            }

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
