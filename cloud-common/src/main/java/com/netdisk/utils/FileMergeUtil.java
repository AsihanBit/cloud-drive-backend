package com.netdisk.utils;

import com.netdisk.constant.MessageConstant;
import com.netdisk.exception.FileChunkException;
import com.netdisk.properties.DiskProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Data
@AllArgsConstructor
@Slf4j
public class FileMergeUtil {

    private String storagePath;
    private String tempDir;


    public static String fileMD5(MultipartFile file) {
        byte[] fileBytes = null;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return DigestUtils.md5DigestAsHex(fileBytes);
    }


    /**
     * 存储分片到临时目录
     *
     * @param file        分片文件
     * @param chunkNumber 分片编号
     * @param subDir      临时目录
     * @throws IOException 如果存储过程中发生 I/O 错误
     */
    public void storeChunk(MultipartFile file, String fileHash, int chunkNumber, String chunkHash, Path subDir) {
        if (file == null || chunkHash == null || chunkHash.length() < 2) {
            throw new FileChunkException(MessageConstant.INVALID_INPUT_PARAMETERS);
        }

        // 创建存储目录路径和临时目录路径
        Path storageDir = Paths.get(storagePath); // E:/cloudfile
        Path defaultTempDir = Paths.get(tempDir); // E:/cloudfile/temp

        // 创建完整的存储路径 重载方法传入哈希值
        Path completeTempDir = storageDir.resolve(defaultTempDir).resolve(subDir);

        try {
            // 检查并创建目录（包括所有必要的父目录）
            if (!Files.exists(completeTempDir)) {
                Files.createDirectories(completeTempDir);
                System.out.println("目录创建成功: " + completeTempDir.toString());
            } else {
                System.out.println("目录已存在: " + completeTempDir.toString());
            }

        } catch (Exception e) {
            throw new FileChunkException(MessageConstant.FAILED_TO_CREATE_SUB_DIR);
        }

        // 构建分片文件路径，名称为 "md5值+chunkNumber"
        Path chunkFilePath = completeTempDir.resolve(fileHash + "_part" + chunkNumber + "_" + chunkHash);
        // String.format("%05d", chunkNumber)

        // 存储分片到指定路径
        try {
            Files.copy(file.getInputStream(), chunkFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileChunkException(MessageConstant.FAILED_TO_STORE_CHUNK);
        }

    }

    // 不带有 tempDir 参数的方法, 使用默认的临时目录
    public void storeChunk(MultipartFile file, String fileHash, int chunkNumber, String chunkHash) {
        if (file == null || chunkHash == null || chunkHash.length() < 2) {
            throw new FileChunkException(MessageConstant.INVALID_INPUT_PARAMETERS);
        }

        // 使用哈希值作为子目录名
//        String hashPrefix = hash.substring(0, 2);
//        String hashPrefix = fileHash;
        Path subDirectory = Paths.get(fileHash);
//        Path temp = Paths.get(tempDir);
//        Path completeTempDir = temp.resolve(subDirectory);

        // 存储文件块
        storeChunk(file, fileHash, chunkNumber, chunkHash, subDirectory);

    }

}
// 缺少tempDir校验  检查 hash 的长度是否足够提取前两位