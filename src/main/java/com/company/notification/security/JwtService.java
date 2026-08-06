//package com.company.notification.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//
///**
// * Resolves the mobileNumber identity from incoming JWTs (issued by an upstream Auth/User
// * service). This service does NOT issue tokens - it only validates and reads claims,
// * keeping the Notification Service stateless w.r.t. authentication.
// */
//@Slf4j
//@Service
//public class JwtService {
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Value("${jwt.mobile-claim:mobileNumber}")
//    private String mobileClaim;
//
//    private SecretKey key() {
//        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//    }
//
//    public boolean isValid(String token) {
//        try {
//            Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
//            return true;
//        } catch (Exception e) {
//            log.warn("Invalid JWT: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    public String extractMobile(String token) {
//        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
//        Object claim = claims.get(mobileClaim);
//        return claim != null ? claim.toString() : claims.getSubject();
//    }
//}
