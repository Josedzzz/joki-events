package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final AdminRepository adminRepository;
    private final ClientRepository clientRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Check if the token is valid
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String getClientToken(UserDetails client) {
        Client aux = new Client();
        Map<String, Object> extraClaims = new HashMap<>();
        if (client instanceof Client clientDetails) {
            String role = clientDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse("CLIENT");
            extraClaims.put("role", role);
            aux = clientDetails;
        }
        return getTokenWithClaims(extraClaims, aux);
    }

    @Override
    public String getAdminToken(UserDetails admin) {
        Admin aux = new Admin();
        Map<String, Object> extraClaims = new HashMap<>();
        if (admin instanceof Admin adminDetails) {
            String role = adminDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst().orElse("ADMIN");
            extraClaims.put("role", role);
            aux = adminDetails;
        }
        return getTokenWithClaims(extraClaims, aux);
    }

    private String getTokenWithClaims(Map<String, Object> extraClaims, UserDetails user) {
        long ACCESS_TOKEN_EXPIRATION = 1440 * 60 * 1000; // 1 day
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    // Generate refresh token with longer expiration time
    public String generateRefreshToken(UserDetails user) {
        long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 30; // 30 days
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("USER"));
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject(); // or use Claims::getSubject directly
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid JWT token");
        }
    }

    @Override
    public String refreshToken(String token) throws JSONException {
        if (isTokenExpired(token)) {
            throw new RuntimeException("Token has expired. Please log in again.");
        }

        String tokenWithoutPrefix = token.replace("Bearer ", "");
        String email = this.extractEmail(tokenWithoutPrefix);

        // Extract user details (client or admin)
        UserDetails userDetails = loadUserByUsername(email);

        // Generate a new token for the user (client or admin)
        String newToken;
        if (userDetails instanceof Client) {
            newToken = this.getClientToken(userDetails);  // For client
        } else if (userDetails instanceof Admin) {
            newToken = this.getAdminToken(userDetails);  // For admin
        } else {
            throw new RuntimeException("Unsupported user type");
        }
        return newToken;
    }

    private String extractEmail(String tokenWithoutPrefix) throws JSONException {
        // Split token into its parts
        String[] parts = tokenWithoutPrefix.split("\\.");

        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token structure");
        }

        // Base64 decode the second part (the payload)
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        // Convert payload to a JSON object to extract the email (or other claims)
        JSONObject jsonPayload = new JSONObject(payload);

        // Use optString() to safely extract the "sub" claim (or email) from the payload
        return jsonPayload.getString("sub"); // The "sub" claim is typically used for the username (email)
    }


    private UserDetails loadUserByUsername(String username) {
        // First, attempt to load a client (could be a normal user)
        Client client = clientRepository.findByEmail(username).orElse(null);

        if (client != null) {
            return new org.springframework.security.core.userdetails.User(
                    client.getEmail(),
                    client.getPassword(),
                    client.getAuthorities()
            );
        }

        // If not a client, attempt to load an admin
        Admin admin = adminRepository.findByEmail(username).orElse(null);

        if (admin != null) {
            return new org.springframework.security.core.userdetails.User(
                    admin.getEmail(),
                    admin.getPassword(),
                    admin.getAuthorities()
            );
        }
        // If user not found, throw exception
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

}
