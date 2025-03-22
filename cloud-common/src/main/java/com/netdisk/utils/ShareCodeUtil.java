package com.netdisk.utils;

import java.util.Random;

public class ShareCodeUtil {
    // 定义提取码的字符集（大小写字母 + 数字）
//    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz123456789";

    // 定义提取码的长度
    private static final int CODE_LENGTH = 4;

    // 随机数生成器
    private static final Random random = new Random();

    /**
     * 生成提取码
     *
     * @return 生成的提取码
     */
    public static String generateShareCode() {
        StringBuilder shareCode = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            // 从字符集中随机选择一个字符
            int index = random.nextInt(CHARACTERS.length());
            shareCode.append(CHARACTERS.charAt(index));
        }
        return shareCode.toString();
    }

    public static void main(String[] args) {
        // 测试生成提取码
        for (int i = 0; i < 5; i++) {
            System.out.println("生成的提取码: " + generateShareCode());
        }
    }
}
