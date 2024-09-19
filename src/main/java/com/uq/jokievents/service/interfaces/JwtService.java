package com.uq.jokievents.service.interfaces;
import com.uq.jokievents.model.Client;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
// Hayao Matsumura
public interface JwtService {

    // Generate a token from Authentication
    String generateToken(Authentication auth);

    // Generate Access Token to a Client or Admin
    String generateAccessToken(String username, Map<String, Object> claims);

    // Generate Refresh Token (used to get new access tokens)
    String generateRefreshToken(String username);

    // Extract username from token
    String extractUsername(String token);

    // Extract expiration date from token
    Date extractExpiration(String token);

    // Generic method to extract claims from a token
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    // Refresh a token using refresh token
    String refreshToken(String refreshToken);

    // Validate token
    Boolean validateToken(String token, String username);

    // Check if a token is a refresh token
    boolean isRefreshToken(String token);

    // Refresh an Access Token using a valid Refresh Token
    String refreshAccessToken(String refreshToken, String username);

    // Generate a client token
    String getClientToken(UserDetails client);
}
