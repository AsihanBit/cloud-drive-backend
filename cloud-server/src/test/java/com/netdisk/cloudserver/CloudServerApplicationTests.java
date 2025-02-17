package com.netdisk.cloudserver;

import com.netdisk.constant.MessageConstant;
import com.netdisk.exception.FileChunkException;
import com.netdisk.properties.DiskProperties;
import org.junit.jupiter.api.Test;
import org.opentest4j.FileInfo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

//@SpringBootTest
class CloudServerApplicationTests {

    private DiskProperties diskProperties;

    public CloudServerApplicationTests(DiskProperties diskProperties) {
        this.diskProperties = diskProperties;
    }


    @Test
    void contextLoads() {
    }

    @Test
    public void getFileMD5() throws IOException {

        FileInputStream fis = new FileInputStream(new File("E:\\cloudfile\\butter.jpg"));
        String md5Str = DigestUtils.md5DigestAsHex(fis);
        System.out.println(md5Str);


    }

    @Test
    public void createSubDir() {

        // 使用哈希值的前两位作为子目录名
        String hashPrefix = "aaaaa";
        Path subDirectory = Paths.get(hashPrefix);

        // 存储文件块

        // 设置默认的临时目录路径
        Path defaultTempDir = Paths.get(diskProperties.getTempDir());

        // 创建完整的存储路径（defaultTempDir/tempDir）
        Path completeTempDir = defaultTempDir.resolve(subDirectory);

        if (!Files.exists(completeTempDir)) {
            // 创建子目录
            try {
                Files.createDirectories(completeTempDir);
            } catch (IOException e) {
                throw new FileChunkException(MessageConstant.FAILED_TO_CREATE_SUB_DIR);
            }
        }

        // 构建分片文件路径，名称为 "md5值+chunkNumber"
        Path chunkFilePath = completeTempDir.resolve(hashPrefix + "_" + String.format("%05d", "1"));


    }

}
