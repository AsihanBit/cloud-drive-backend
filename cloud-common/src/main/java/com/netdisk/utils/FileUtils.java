package com.netdisk.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUtils {

    // 根据文件扩展名设置对应的 Content-Type
    public static String getContentType(String extension) {
        switch (extension.toLowerCase()) {
            case "txt":
                return "text/plain";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "pdf":
                return "application/pdf";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            // 添加其他需要的文件类型
            default:
                return "application/octet-stream"; // 默认的二进制流文件类型
        }
    }

    // 获取文件扩展名
    public static String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return ""; // 无扩展名
        }
        return filename.substring(lastIndexOfDot + 1);
    }
}
