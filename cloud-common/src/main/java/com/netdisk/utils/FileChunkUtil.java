package com.netdisk.utils;

import com.netdisk.constant.MessageConstant;
import com.netdisk.exception.FileChunkException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Slf4j
public class FileChunkUtil {

    private String storagePath;
    private String tempDir;


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


    /**
     * 合并分片文件
     *
     * @param fileHash 文件的哈希值（或唯一标识）
     * @throws IOException 如果合并过程中发生 I/O 错误
     */
    public void mergeChunks(String fileHash) throws IOException {
        // 获取临时目录下对应文件的所有分片
        Path completeTempDir = Paths.get(storagePath).resolve(tempDir).resolve(fileHash);
        List<Path> chunkFiles = Files.list(completeTempDir)
//                .filter(path -> path.toString().startsWith(fileHash))
                .filter(path -> path.getFileName().toString().matches(fileHash + "_part\\d+_.*"))
                .sorted(createCustomComparator()) // 确保按正确的顺序合并
                .collect(Collectors.toList());

        // 目标文件路径
        Path targetFilePath = Paths.get(storagePath).resolve(fileHash + "_merged");

        // 创建或覆盖目标文件并合并分片
        try (OutputStream outputStream = Files.newOutputStream(targetFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Path chunk : chunkFiles) {
                try (InputStream inputStream = Files.newInputStream(chunk)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    // 显式调用 flush() 确保数据被写出
                    outputStream.flush();
                }
                // 删除已处理的分片
//                Files.delete(chunk);
            }
        }

        // 可选：删除已合并的分片以节省空间
//        for (Path chunk : chunkFiles) {
//            Files.delete(chunk);
//        }

        // 可选：删除空的临时目录
//        Files.deleteIfExists(completeTempDir);
    }

    /**
     * 创建自定义比较器，用于根据文件名中的数字前缀进行排序
     *
     * @return 自定义比较器
     */
    private Comparator<Path> createCustomComparator() {
        return (path1, path2) -> {
            String name1 = path1.getFileName().toString();
            String name2 = path2.getFileName().toString();

            // 提取文件名中的数字部分
            int index1 = extractChunkNumber(name1);
            int index2 = extractChunkNumber(name2);

            return Integer.compare(index1, index2);
        };
    }

    /**
     * 从文件名中提取分片编号
     *
     * @param fileName 文件名
     * @return 分片编号
     */
    private int extractChunkNumber(String fileName) {
        // 使用正则表达式提取分片编号
        String pattern = "(?<=_part)(\\d+)(?=_)";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(fileName);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalArgumentException("无法从文件名中提取分片编号: " + fileName);
        }
    }

    public static String fileMD5(MultipartFile file) {
        byte[] fileBytes = null;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return DigestUtils.md5DigestAsHex(fileBytes);
    }

}
// 缺少tempDir校验  检查 hash 的长度是否足够提取前两位