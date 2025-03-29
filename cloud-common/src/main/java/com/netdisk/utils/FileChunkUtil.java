package com.netdisk.utils;

import com.netdisk.constant.MessageConstant;
import com.netdisk.entity.MergeFileResult;
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
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@Slf4j
public class FileChunkUtil {

    private String storagePath;
    private String fileDir;
    private String tempDir;


    /**
     * 分片保存时:  storagePath/tempDir/ 整文件md5 / part_1_分片md5
     * 检查存在时:  先拿到 [getChunkDirPath] storagePath/tempDir/整文件md5   再取 _ [2]
     *
     */

    /**
     * 根据传入的 MD5 值生成完整的文件路径
     *
     * @param fileHash MD5 值
     * @return 完整的文件路径
     */
    public Path getFileCompletePath(String fileHash) {
        if (fileHash == null || fileHash.length() < 2) {
            throw new IllegalArgumentException("传入的 MD5 值无效，长度至少为 2");
        }
        // 提取 MD5 值的前两位
        String firstTwoChars = fileHash.substring(0, 2);

        // 构建包含 MD5 前两位子目录的完整路径
        return Paths.get(storagePath, fileDir, firstTwoChars, fileHash);
//        return Paths.get(storagePath, firstTwoChars);
    }

    public Path getFileDirPath(String fileHash) {
        if (fileHash == null || fileHash.length() < 2) {
            throw new IllegalArgumentException("传入的 MD5 值无效，长度至少为 2");
        }
        // 提取 MD5 值的前两位
        String firstTwoChars = fileHash.substring(0, 2);

        // 构建包含 MD5 前两位子目录的完整路径
        return Paths.get(storagePath, fileDir, firstTwoChars);
//        return Paths.get(storagePath, firstTwoChars);
    }

    public Path getChunkDirPath(String fileHash) {
        if (fileHash == null || fileHash.length() < 2) {
            throw new IllegalArgumentException("传入的 MD5 值无效，长度至少为 2");
        }
        // 构建分片路径：storagePath\tempDir\完整文件md5
        return Paths.get(storagePath, tempDir, fileHash);
    }

    public Path getChunkCompletePath(String fileHash, String chunkHash, Integer chunkNumber) {
        if (fileHash == null || fileHash.length() < 2) {
            throw new IllegalArgumentException("传入的 MD5 值无效，长度至少为 2");
        }
        if (chunkNumber == null) {
            throw new IllegalArgumentException("分片编号不能为空");
        }


        // 构建分片路径：storagePath\tempDir\完整文件md5
//        return Paths.get(storagePath, tempDir, fileHash, chunkHash);
        // 构建分片路径和文件名：storagePath/tempDir/fileHash/part_chunkNumber_chunkHash
        return Paths.get(storagePath, tempDir, fileHash, "part_" + chunkNumber + "_" + chunkHash);
    }

    /**
     * 分片存在性检查 - 存储式
     */
    public boolean checkChunkExistsLS(String fileHash, String chunkHash, Integer chunkNumber) {
        Path chunkPath = getChunkCompletePath(fileHash, chunkHash, chunkNumber);
        // 检查分片文件是否存在
        return Files.exists(chunkPath);
    }

    /**
     * 文件存在性检查 - 存储式
     */
    public boolean checkFileExistsLS(String md5) {
        Path filePath = getFileCompletePath(md5);
        // 检查文件是否存在
        return Files.exists(filePath);
    }


    /**
     * 存储分片到临时目录
     *
     * @param file        分片文件
     * @param chunkNumber 分片编号
     * @throws IOException 如果存储过程中发生 I/O 错误
     */
    public void storeChunk(MultipartFile file, String fileHash, int chunkNumber, String chunkHash) {
        if (file == null || chunkHash == null || chunkHash.length() < 2) {
            throw new FileChunkException(MessageConstant.INVALID_INPUT_PARAMETERS);
        }

        // 创建临时文件存储目录路径
        // TODO 以后在单独分出来 在新文件判断里执行
        Path chunkPath = getChunkDirPath(fileHash);
        try {
            // 检查并创建目录（包括所有必要的父目录）
            if (!Files.exists(chunkPath)) {
                Files.createDirectories(chunkPath);
                System.out.println("目录创建成功: " + chunkPath.toString());
            } else {
                System.out.println("目录已存在: " + chunkPath.toString());
            }
        } catch (Exception e) {
            throw new FileChunkException(MessageConstant.FAILED_TO_CREATE_SUB_DIR);
        }

        // 构建分片文件路径，名称为 "整个文件md5_part1_分片md5"
//        Path chunkFilePath = chunkPath.resolve(fileHash + "_part" + chunkNumber + "_" + chunkHash);
//        Path chunkFilePath = chunkPath.resolve("part_" + chunkNumber + "_" + chunkHash);
        Path chunkFilePath = getChunkCompletePath(fileHash, chunkHash, chunkNumber);

//        Path chunkFilePath = chunkPath.resolve(chunkHash);

        // 存储分片到指定路径
        try {
            Files.copy(file.getInputStream(), chunkFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileChunkException(MessageConstant.FAILED_TO_STORE_CHUNK);
        }

    }


    /**
     * 合并分片文件
     *
     * @param fileHash 文件的哈希值（或唯一标识）
     * @throws IOException 如果合并过程中发生 I/O 错误
     */
    public MergeFileResult mergeChunks(String fileHash) {
        // 获取临时目录下对应文件的所有分片
//        Path completeTempDir = Paths.get(storagePath).resolve(tempDir).resolve(fileHash);
        Path completeTempDir = getChunkDirPath(fileHash);
        List<Path> chunkFiles = null;
        try {
            chunkFiles = Files.list(completeTempDir)
                    .filter(path -> path.getFileName().toString().startsWith("part_"))
                    //                .filter(path -> path.getFileName().toString().matches(fileHash + "_part\\d+_.*"))
                    //                .filter(path -> path.getFileName().toString().matches("part_(\\d+)_.*"))
                    .sorted(createCustomComparator()) // 确保按正确的顺序合并
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.info("要合并的文件不存在异常");
            throw new RuntimeException(e);
        }

        // 目标文件路径
        Path fileDirPath = getFileDirPath(fileHash);
        try {
            // 检查并创建目录（包括所有必要的父目录）
            if (!Files.exists(fileDirPath)) {
                Files.createDirectories(fileDirPath);
                System.out.println("目录创建成功: " + fileDirPath.toString());
            } else {
                System.out.println("目录已存在: " + fileDirPath.toString());
            }
        } catch (Exception e) {
            throw new FileChunkException(MessageConstant.FAILED_TO_CREATE_SUB_DIR);
        }

        Path filePath = fileDirPath.resolve(fileHash);

        // 创建或覆盖目标文件并合并分片
        try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
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
                Files.delete(chunk);
                // 注:这里返回 MergeFileResult 的话,不会每次合并都会输出log.info,只会输出一次,神奇
            }

            // 可选：删除空的临时目录
            Files.deleteIfExists(completeTempDir);

            // 计算合并后文件的 MD5 值
            String md5 = calculateFileMD5(filePath);
            // 计算合并后文件的字节大小
            Long fileSize = Files.size(filePath);
            log.info("合并后的文件 MD5: {}", md5);
            log.info("合并后的文件大小 (字节): {}", fileSize);
            MergeFileResult mergeFileResult = MergeFileResult.builder()
                    .fileHash(md5)
                    .fileSize(fileSize)
                    .build();
            return mergeFileResult;
        } catch (IOException e) {
            log.info("合并出错");
            throw new RuntimeException(e);
        }

        // 可选：删除已合并的分片以节省空间
//        for (Path chunk : chunkFiles) {
//            Files.delete(chunk);
//        }

        // 可选：删除空的临时目录
//        Files.deleteIfExists(completeTempDir);
//        return new MergeFileResult();
    }

    public void deleteAllChunks(String fileHash) {
        // 获取所有分片临时目录
        Path completeTempDir = getChunkDirPath(fileHash);

        // 可选：删除空的临时目录
        try {
            // 删除目录中的所有文件和子目录
            if (Files.isDirectory(completeTempDir)) {
                try (Stream<Path> entries = Files.list(completeTempDir)) {
                    for (Path entry : entries.toArray(Path[]::new)) {
                        Files.deleteIfExists(entry);
                    }
                }
            }
            // 删除空目录
            Files.deleteIfExists(completeTempDir);
        } catch (IOException e) {
            log.info("删除操作:获取目录失败");
            throw new RuntimeException(e);
        }

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
//        String pattern = "(?<=_part)(\\d+)(?=_)";
        String pattern = "part_(\\d+)_";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(fileName);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalArgumentException("无法从文件名中提取分片编号: " + fileName);
        }
    }


    private String calculateFileMD5(Path filePath) {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            // 使用 DigestUtils.md5DigestAsHex 计算文件的 MD5 值
            return DigestUtils.md5DigestAsHex(inputStream);
        } catch (IOException e) {
            log.info("计算md5出错");
            throw new RuntimeException(e);
        }
    }

    // 保存分片时, redis中保存分片信息
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