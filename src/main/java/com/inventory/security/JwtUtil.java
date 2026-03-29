package com.inventory.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

//JwtUtil.java
@Component
public class JwtUtil {
 @Value("${jwt.secret}")       private String secret;
 @Value("${jwt.expiration-ms}") private long expirationMs;

 private SecretKey getKey() {
     return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
 }

 public String generateToken(UserDetails userDetails) {
     return Jwts.builder()
         .subject(userDetails.getUsername())
         .claim("role", userDetails.getAuthorities().iterator().next().getAuthority())
         .issuedAt(new Date())
         .expiration(new Date(System.currentTimeMillis() + expirationMs))
         .signWith(getKey())
         .compact();
 }

 public String extractUsername(String token) {
     return getClaims(token).getSubject();
 }

 public boolean isTokenValid(String token, UserDetails userDetails) {
     return extractUsername(token).equals(userDetails.getUsername())
         && !getClaims(token).getExpiration().before(new Date());
 }

 private Claims getClaims(String token) {
     return Jwts.parser().verifyWith(getKey()).build()
         .parseSignedClaims(token).getPayload();
 }
}
