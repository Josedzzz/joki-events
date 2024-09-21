package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.service.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
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
    @Value("${jwt.secret}")
    private String SECRET_KEY;


    private Key getSigningKey() {
        // This is the SECRET_KEY for signing tokens. In production, this should be very secret
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
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
                .build().parseSignedClaims(token).getBody();
    }
    // Validate token
    public Boolean validateToken(String token) {
        try {
            // Check if the token is expired
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false; // Return false if any error occurs during validation
        }
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String getClientToken(UserDetails client) {
        Client aux = new Client();
        Map<String, Object> extraClaims = new HashMap<>();

        if (client instanceof Client clientDetails) { // This makes sense for reallllll
            String role = clientDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse("CLIENT");

            extraClaims.put("role", role);
            aux = clientDetails;
        }
        return getClientToken(extraClaims, aux);
    }

    private String getClientToken(Map<String,Object> extraClaims, UserDetails client) {
        return Jwts.builder().claims(extraClaims).subject(client.getUsername()).issuedAt(new Date(System.currentTimeMillis())).expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    @Override
    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getUserIdFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }
}
