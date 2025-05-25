package com.netdisk.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class CipherUtils {
    // TODO 将key等 yml配置化
    private static final String ALGORITHM = "AES";
    // AES 算法支持的密钥长度为 16 字节（128 位）、24 字节（192 位）或者 32 字节（256 位）
//    private static final String KEY = "CloudDriveSecret";
    private static final String KEY = "t2GhL6pQs9KwDjEbVmNcYxZoAiUrTfWc";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    // 建议配置化放到 yml
    private static final int IV_LENGTH = 16;

    private CipherUtils() {
        throw new UnsupportedOperationException("工具类禁止实例化");
    }

    public static String encryptECB(int number) throws Exception {
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

    public static int decryptECB(String encryptedString) throws Exception {

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

    public static String encryptCBC(int number) throws Exception {
        byte[] ivBytes = generateIV();
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        byte[] encryptedBytes = cipher.doFinal(String.valueOf(number).getBytes(StandardCharsets.UTF_8));

        // 拼接 IV + 加密内容，统一编码为 Base64
//        byte[] combined = new byte[ivBytes.length + encryptedBytes.length];
//        System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
//        System.arraycopy(encryptedBytes, 0, combined, ivBytes.length, encryptedBytes.length);
        // 合并 IV 和密文，进行 Base64 URL 编码
        byte[] combined = ByteBuffer.allocate(ivBytes.length + encryptedBytes.length)
                .put(ivBytes).put(encryptedBytes).array();

        return Base64.getUrlEncoder().withoutPadding().encodeToString(combined); // 适合 URL 的分享码
    }

    public static int decryptCBC(String encryptedString) throws Exception {
        byte[] combined = Base64.getUrlDecoder().decode(encryptedString);

        byte[] ivBytes = new byte[IV_LENGTH];
        byte[] encryptedBytes = new byte[combined.length - IV_LENGTH];

        System.arraycopy(combined, 0, ivBytes, 0, IV_LENGTH);
        System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

        IvParameterSpec iv = new IvParameterSpec(ivBytes);
        SecretKeySpec secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return Integer.parseInt(new String(decryptedBytes, StandardCharsets.UTF_8));
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }


}
