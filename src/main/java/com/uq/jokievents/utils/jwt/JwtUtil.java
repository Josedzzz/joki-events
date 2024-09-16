package com.uq.jokievents.utils.jwt;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
// Hayao Matsumura
@Component
public class JwtUtil {

    // Short-lived Access Token expiration time, for now it is 15 mins long
    private final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;

    private Key getSigningKey() {
        // This is the SECRET_KEY for signing tokens. In production, this should be very secret
        String SECRET_KEY = "very-strong-secret-key-that-should-be-very-long-but-right-now-it-isn't"; // System.getEnv()
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate Access Token to a Client or Admin
    public String generateAccessToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Generate Refresh Token (used to get new access tokens)
    public String generateRefreshToken(String username) {
        // Refresh Token expiration time of a week
        long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000;
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract claims from a token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate token
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    // Check if a token is a refresh token. Will be used for requests
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().getTime() - claims.getIssuedAt().getTime() > ACCESS_TOKEN_EXPIRATION;
        } catch (Exception e) {
            return false;
        }
    }

    // Refresh an Access Token using a valid Refresh Token
    public String refreshAccessToken(String refreshToken, String username) {
        if (isTokenExpired(refreshToken)) {
            throw new IllegalStateException("Refresh token is expired.");
        }
        return generateAccessToken(username, new HashMap<>());
    }
}
