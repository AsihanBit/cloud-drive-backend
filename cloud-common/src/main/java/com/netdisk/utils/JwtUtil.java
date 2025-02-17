package com.netdisk.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.AeadAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureAlgorithm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Data
@AllArgsConstructor
@Slf4j
public class JwtUtil {
    // 签名密钥, 从配置文件读取
    private String userSecretKey;
    private Long userTtl;
    private String userTokenName;

    /**
     * 生成JWT token
     * 参数改成: Map<String, Object> claims ?
     */
    public String createJwt(String subject) {
        AeadAlgorithm enc = Jwts.ENC.A256GCM;
        SecretKey encryptionKey = enc.key().build();

        // 从字符串生成密钥
        SecretKey secretKey = Keys.hmacShaKeyFor(userSecretKey.getBytes(StandardCharsets.UTF_8));

        // 将Long类型的毫秒值转换为Date对象
        Date expirationDate = new Date(System.currentTimeMillis() + userTtl);

        String jwtToken = Jwts.builder()
                .subject(subject)
                .expiration(expirationDate)
                .signWith(secretKey)
//                .encryptWith(encryptionKey, enc)
                .compact();

        return jwtToken; // 返回生成的JWT token
    }

    /**
     * 解析JWT token
     */
    public Jws<Claims> parseJwt(String token) {
        // 从字符串生成密钥
        SecretKey secretKey = Keys.hmacShaKeyFor(userSecretKey.getBytes(StandardCharsets.UTF_8));

        Jws<Claims> jws = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        // 解析失败会报错?
        return jws;
    }

}
