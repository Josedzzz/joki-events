package com.uq.jokievents.service.implementation;

import com.uq.jokievents.service.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    // Short-lived Access Token expiration time, for now it is 15 mins long
    private final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000;
    private final String SECRET_KEY = "very-strong-secret-key-that-should-be-very-long-but-right-now-it-isn't"; // System.getEnv()


    private Key getSigningKey() {
        // This is the SECRET_KEY for signing tokens. In production, this should be very secret
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Generate a token from Authentication
    public String generateToken(Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();  // Extract user details from auth
        Map<String, Object> claims = new HashMap<>();  // Claims can hold additional data, like roles if needed
        return generateAccessToken(userDetails.getUsername(), claims);
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

    public String refreshToken(String refreshToken) {
        // Validate the refresh token
        if (!isRefreshToken(refreshToken)) {
            // Extract claims and generate a new access token
            Claims claims = extractAllClaims(refreshToken);
            // Generate a new token based on the same user details
            return generateTokenWithClaims(claims);
        } else {
            throw new IllegalArgumentException("Invalid refresh token");
        }
    }

    private String generateTokenWithClaims(Claims claims) {
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
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

    public String getClientToken(UserDetails client) {
        return getClientToken(new HashMap<>(), client);
    }

    private String getClientToken(Map<String,Object> extraClaims, UserDetails client) {
        return Jwts.builder().claims(extraClaims).subject(client.getUsername()).issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }


}
