package com.netdisk.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CipherUtils {
    // TODO 将key等 yml配置化
    private static final String ALGORITHM = "AES";
    // AES 算法支持的密钥长度为 16 字节（128 位）、24 字节（192 位）或者 32 字节（256 位）
    private static final String KEY = "CloudDriveSecret";

    private CipherUtils() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    public static String encrypt(int number) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedBytes = cipher.doFinal(String.valueOf(number).getBytes(StandardCharsets.UTF_8));
        StringBuilder encryptedString = new StringBuilder(Base64.getEncoder().encodeToString(encryptedBytes));

        // 填充到固定长度，假设固定长度为32
//        while (encryptedString.length() < 32) {
//            encryptedString.append("=");
//        }

        return encryptedString.toString();
    }

    public static int decrypt(String encryptedString) throws Exception {

        // 填充字符: 去除多余 补齐正确
        StringBuilder encryptedStringBuilder = new StringBuilder(encryptedString.replaceAll("=", ""));
        while (encryptedStringBuilder.length() % 4 != 0) {
            encryptedStringBuilder.append("=");
        }
        encryptedString = encryptedStringBuilder.toString();


        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] decodedBytes = Base64.getDecoder().decode(encryptedString);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);

        return Integer.parseInt(new String(decryptedBytes, StandardCharsets.UTF_8));
    }


}
