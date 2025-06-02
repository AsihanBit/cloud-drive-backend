package com.netdisk.cloudserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.system.UserInfo;
import com.netdisk.cloudserver.mapper.UserFilesMapper;
import com.netdisk.cloudserver.mapper.UserMapper;
import com.netdisk.cloudserver.service.UserFilesService;
import com.netdisk.cloudserver.service.UserService;
import com.netdisk.constant.MessageConstant;
import com.netdisk.constant.StatusConstant;
import com.netdisk.context.BaseContext;
import com.netdisk.dto.UserAccountStatusDTO;
import com.netdisk.dto.UserDTO;
import com.netdisk.dto.UserLoginDTO;
import com.netdisk.dto.UserRegisterDTO;
import com.netdisk.entity.File;
import com.netdisk.entity.User;
import com.netdisk.entity.UserFiles;
import com.netdisk.exception.AccountNotFoundException;
import com.netdisk.exception.BaseException;
import com.netdisk.exception.PasswordErrorException;
import com.netdisk.exception.UserAlreadyExistException;
import com.netdisk.utils.ElasticSearchUtils;
import com.netdisk.vo.UserInfoVO;
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
    private UserFilesService userFilesService;
    private UserFilesMapper userFilesMapper;
    private ElasticSearchUtils elasticSearchUtils;

    public UserServiceImpl(
            UserFilesService userFilesService,
            UserMapper userMapper,
            UserFilesMapper userFilesMapper,
            ElasticSearchUtils elasticSearchUtils) {
        this.userFilesService = userFilesService;
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
        if (user.getStatus().equals(StatusConstant.ACCOUNT_STATUS_LOCKED)) {
            throw new PasswordErrorException(MessageConstant.ACCOUNT_LOCKED);
        }
        // 更新登录时间
        UserDTO userDTO = UserDTO.builder()
                .userId(user.getUserId())
                .lastLoginTime(LocalDateTime.now())
                .build();
        userMapper.updateUser(userDTO);
        user.setPassword(null);

        return user;
    }

    /**
     * 查询用户列表
     *
     * @return
     */
    @Override
    public List<UserInfoVO> getUserInfoList() {
        List<User> userList = userMapper.selectUserInfoList();
        List<UserInfoVO> voList = BeanUtil.copyToList(userList, UserInfoVO.class);
        return voList;
    }

    /**
     * id查询用户
     *
     * @param userId
     * @return
     */
    @Override
    public User getUserById(Integer userId) {
        User user = userMapper.selectUserByUserId(userId);
        return user;
    }

    /**
     * 添加用户
     *
     * @param userRegisterDTO
     */
    @Override
    public void addUser(UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO.getUsername() == null || userRegisterDTO.getUsername().isEmpty()) {
            throw new BaseException("用户名为空");
        }
        if (userRegisterDTO.getPassword() == null || userRegisterDTO.getPassword().isEmpty()) {
            throw new BaseException("密码为空");
        }
        if (userRegisterDTO.getNickname() == null || userRegisterDTO.getNickname().isEmpty()) {
            userRegisterDTO.setNickname(userRegisterDTO.getUsername());
        }
        if (userRegisterDTO.getVip() != 0 && userRegisterDTO.getVip() != 1) {
            userRegisterDTO.setVip(0);
        }
        if (userRegisterDTO.getTotalSpace() == null) {
            userRegisterDTO.setTotalSpace(1073741824L);
        }
        userRegisterDTO.setUsedSpace(0L);
        LocalDateTime now = LocalDateTime.now();
        userRegisterDTO.setRegisterTime(now);
        userRegisterDTO.setStatus(StatusConstant.ACCOUNT_STATUS_NORMAL);
        userMapper.insertUser(userRegisterDTO);
    }


    /**
     * 修改账户状态
     *
     * @param userAccountStatusDTO
     */
    @Override
    public void modifyUserAccountStatus(UserAccountStatusDTO userAccountStatusDTO) {

        // todo 不用传status
        User user = userMapper.selectUserByUserId(userAccountStatusDTO.getUserId());
        if (user.getUserId() == null) {
            throw new BaseException("账户状态修改失败");
        }
        if (user.getStatus() != StatusConstant.ACCOUNT_STATUS_NORMAL && user.getStatus() != StatusConstant.ACCOUNT_STATUS_LOCKED) {
            throw new BaseException("账户状态修改失败");
        }
        if (user.getStatus() == StatusConstant.ACCOUNT_STATUS_NORMAL) {
            // 如果账户状态正常,则封禁
            userAccountStatusDTO.setStatus(StatusConstant.ACCOUNT_STATUS_LOCKED);
        }
        if (user.getStatus() == StatusConstant.ACCOUNT_STATUS_LOCKED) {
            // 如果账户状态封禁,则正常
            userAccountStatusDTO.setStatus(StatusConstant.ACCOUNT_STATUS_NORMAL);
        }
        userMapper.updateAccountStatus(userAccountStatusDTO);
    }

    /**
     * 删除用户账户
     *
     * @param userId
     */
    @Override
    public void deleteUserAccountByUserId(Integer userId) {
        userMapper.deleteUserAccountByUserId(userId);
    }

    /**
     * 修改用户信息
     *
     * @param userDTO
     */
    @Override
    public void updateUser(UserDTO userDTO) {
        userMapper.updateUser(userDTO);
    }

    /**
     * 判断剩余空间是否足够可用
     *
     * @param fileSize
     * @return
     */
    @Override
    public boolean checkSpaceEnough(Long fileSize) {
        Integer userId = BaseContext.getCurrentId();
        User user = userMapper.selectUserByUserId(userId);
        if (user.getUsedSpace() + fileSize > user.getTotalSpace()) {
            return false;
        }
        return true;
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
                new UserFiles(null, userId, "文件示例-点击在线预览", (short) 0, 0, (short) 1, null, null, null, null, now, now, (short) 1, (short) 1),
                new UserFiles(null, userId, "ik分词器jar包.jar", (short) 1, 0, (short) 1, 349, 47931L, "/", "jar", now, now, (short) 1, (short) 1),
                // 文本文档
                new UserFiles(null, userId, "维基文档.txt", (short) 1, 0, (short) 1, 350, 2916L, "/", "txt", now, now, (short) 1, (short) 1),
                new UserFiles(null, userId, "SpringBoot面试题.md", (short) 1, 0, (short) 1, 351, 18947L, "/", "md", now, now, (short) 1, (short) 1),
                new UserFiles(null, userId, "Redis工具类.java", (short) 1, 0, (short) 1, 352, 2714L, "/", "java", now, now, (short) 1, (short) 1),
                new UserFiles(null, userId, "Sql建表.sql", (short) 1, 0, (short) 1, 353, 2884L, "/", "sql", now, now, (short) 1, (short) 1),

                // Office文档
                new UserFiles(null, userId, "java基础总结.doc", (short) 1, 0, (short) 1, 354, 369152L, "/", "doc", now, now, (short) 1, (short) 1),
                new UserFiles(null, userId, "物理循环课表-线下.xlsx", (short) 1, 0, (short) 1, 355, 32234L, "/", "xlsx", now, now, (short) 1, (short) 1),
                new UserFiles(null, userId, "linux命令(14题).pdf", (short) 1, 0, (short) 1, 356, 233134L, "/", "pdf", now, now, (short) 1, (short) 1),
                new UserFiles(null, userId, "项目模块pom.xml", (short) 1, 0, (short) 1, 357, 4537L, "/", "xml", now, now, (short) 1, (short) 1),

                // 压缩包
                new UserFiles(null, userId, "图片-压缩包.zip", (short) 1, 0, (short) 1, 358, 3977338L, "/", "zip", now, now, (short) 1, (short) 1)
        );

        // 转换为 UserFiles 并批量插入
        List<UserFiles> userFilesList = defaultFiles.stream()
                .map(file -> buildUserFile(userId, now, file))
                .collect(Collectors.toList());

        // 计算所有实际文件的总大小（排除文件夹）
        Long totalFileSize = defaultFiles.stream()
                .filter(file -> file.getItemType() == (short) 1) // 只计算文件，排除文件夹
                .mapToLong(file -> file.getFileSize() != null ? file.getFileSize() : 0L)
                .sum();
        userFilesService.updateUsedSpace(userId, totalFileSize);
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
