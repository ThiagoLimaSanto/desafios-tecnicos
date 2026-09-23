package com.thiagolima.desafio_backend_clube_do_Java.service;

import java.util.Date;
import java.util.HexFormat;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.thiagolima.desafio_backend_clube_do_Java.enums.UserRole;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
    private final SecretKey key;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(HexFormat.of().parseHex(secret));
    }

    public String generateToken(String email, UserRole role, String name) {
        long expiration = 1000 * 60 * 60;

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
