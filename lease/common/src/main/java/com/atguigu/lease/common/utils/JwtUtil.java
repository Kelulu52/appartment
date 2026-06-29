package com.atguigu.lease.common.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {

    private static SecretKey secretKey = Keys.hmacShaKeyFor("mXAt6gKM1p9myk6occC8xOG2cG3GE8us".getBytes());
    public static String cteatToken(long userid, String username) {

        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .setSubject("LOGIN_USER")
                .claim("username", username)
                .claim("userId", userid)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static void main(String[] args) {
        System.out.println(cteatToken(1, "admin"));
    }
}
