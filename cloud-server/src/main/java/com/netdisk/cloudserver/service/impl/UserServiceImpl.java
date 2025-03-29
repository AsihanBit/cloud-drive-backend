package com.netdisk.cloudserver.service.impl;

import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.cloudserver.mapper.UserMapper;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.constant.StatusConstant;
import com.netdisk.dto.UserLoginDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.User;
import com.netdisk.entity.UserFiles;
import com.netdisk.exception.AccountNotFoundException;
import com.netdisk.exception.PasswordErrorException;
import com.netdisk.exception.UserAlreadyExistException;
import com.netdisk.utils.ElasticSearchUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private UserMapper userMapper;
    // 初始化示例文档
    private UserFilesMapper userFilesMapper;
    private ElasticSearchUtils elasticSearchUtils;

    public UserServiceImpl(
            UserMapper userMapper,
            UserFilesMapper userFilesMapper,
            ElasticSearchUtils elasticSearchUtils) {
        this.userMapper = userMapper;
        this.userFilesMapper = userFilesMapper;
        this.elasticSearchUtils = elasticSearchUtils;
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // TODO 验证用户名 密码 格式
        // 验证用户名
        User userExist = userMapper.queryUserByUsername(userRegisterDTO.getUsername());
        if (userExist != null) {
            throw new UserAlreadyExistException(MessageConstant.USERNAME_DUPLICATE);
        }
        // 验证昵称
        if (userRegisterDTO.getNickname() == null || userRegisterDTO.getNickname().isEmpty()) {
            userRegisterDTO.setNickname(userRegisterDTO.getUsername());
        }

        User newUser = User.builder()
                .username(userRegisterDTO.getUsername())
                .password(userRegisterDTO.getPassword())
                .nickname(userRegisterDTO.getNickname())
                .vip(StatusConstant.MEMBER_LEVEL_REGULAR)
                .usedSpace(StatusConstant.USED_STORAGE_SPACE)
                .totalSpace(StatusConstant.TOTAL_STORAGE_SPACE)
                .registerTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .status(StatusConstant.ACCOUNT_STATUS_NORMAL)
                .build();

        userMapper.userRegister(newUser);
        Integer userId = newUser.getUserId();
        // 初始化示例文档
        try {
            // 初始化示例文档（捕获异常不影响主流程）
            insertDefaultFiles(userId);
        } catch (Exception e) {
            log.error("用户注册成功，但初始化示例文档失败，用户ID: {}", userId, e);
            // 可以选择记录到监控系统或发送通知
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO
     */
    @Override
    public User userLogin(UserLoginDTO userLoginDTO) {
        User user = userMapper.queryUserByUsername(userLoginDTO.getUsername());
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (!user.getPassword().equals(userLoginDTO.getPassword())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        user.setPassword(null);

        return user;
    }


    /**
     * 给用户初始示例文档
     *
     * @param userId
     */
    public void insertDefaultFiles(Integer userId) {
        log.info("开始添加 - 初始示例文档");
        LocalDateTime now = LocalDateTime.now();

        // 定义默认文件列表（可扩展）
        List<UserFiles> defaultFiles = Arrays.asList(
                new UserFiles(null, userId, "文件示例-点击在线预览", (short) 0, 0, (short) 1, null, null, null, null, now, now, (short) 0),
                new UserFiles(null, userId, "ik分词器jar包.jar", (short) 1, 0, (short) 1, 349, 47931L, "/", "jar", now, now, (short) 0),
                // 文本文档
                new UserFiles(null, userId, "维基文档.txt", (short) 1, 0, (short) 1, 350, 2916L, "/", "txt", now, now, (short) 0),
                new UserFiles(null, userId, "SpringBoot面试题.md", (short) 1, 0, (short) 1, 351, 18947L, "/", "md", now, now, (short) 0),
                new UserFiles(null, userId, "Redis工具类.java", (short) 1, 0, (short) 1, 352, 2714L, "/", "java", now, now, (short) 0),
                new UserFiles(null, userId, "Sql建表.sql", (short) 1, 0, (short) 1, 353, 2884L, "/", "sql", now, now, (short) 0),

                // Office文档
                new UserFiles(null, userId, "java基础总结.doc", (short) 1, 0, (short) 1, 354, 369152L, "/", "doc", now, now, (short) 0),
                new UserFiles(null, userId, "物理循环课表-线下.xlsx", (short) 1, 0, (short) 1, 355, 32234L, "/", "xlsx", now, now, (short) 0),
                new UserFiles(null, userId, "linux命令(14题).pdf", (short) 1, 0, (short) 1, 356, 233134L, "/", "pdf", now, now, (short) 0),
                new UserFiles(null, userId, "项目模块pom.xml", (short) 1, 0, (short) 1, 357, 4537L, "/", "xml", now, now, (short) 0),

                // 压缩包
                new UserFiles(null, userId, "图片-压缩包.zip", (short) 1, 0, (short) 1, 358, 3977338L, "/", "zip", now, now, (short) 0)
        );

        // 转换为 UserFiles 并批量插入
        List<UserFiles> userFilesList = defaultFiles.stream()
                .map(file -> buildUserFile(userId, now, file))
                .collect(Collectors.toList());

        userFilesMapper.batchInsertItems(userFilesList);
        elasticSearchUtils.bulkInsertUserFiles(userFilesList);

        log.info("用户注册成功 - 初始化示例文档完成");
    }


    /**
     * 构建 UserFiles 对象
     */
    private UserFiles buildUserFile(Integer userId, LocalDateTime now, UserFiles fileInfo) {
        return UserFiles.builder()
                .userId(userId)
                .itemName(fileInfo.getItemName())
                .itemType(fileInfo.getItemType())  // 0文件夹 1文件
                .pId(0)               // 默认根目录
                .directoryLevel((short) 1)  // 默认层级
                .fileId(fileInfo.getFileId())
                .fileSize(fileInfo.getFileSize())
                .fileCover(fileInfo.getFileCover())
                .fileExtension(fileInfo.getFileExtension())
                .upLoadTime(now)
                .updateTime(now)
                .recycleStatus((short) 0)
                .build();
    }
}
