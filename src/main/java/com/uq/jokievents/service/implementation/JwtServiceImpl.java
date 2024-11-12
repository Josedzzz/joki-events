package com.uq.jokievents.service.implementation;

import com.uq.jokievents.config.ApplicationConfig;
import com.uq.jokievents.exceptions.LogicException;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
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
import java.sql.SQLOutput;
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
    @Getter private final ApplicationConfig applicationConfig;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(applicationConfig.getSECRET_JWT_KEY().getBytes());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        try {
            // Extract expiration date
            Date expirationDate = extractClaim(token, Claims::getExpiration);
            // Check if expiration date is before the current date
            // If the expiration is after the current date, means it is active
            return expirationDate.after(new Date());
        } catch (ExpiredJwtException e) {
            return true; // Token is expired if this exception is thrown
        } catch (JwtException e) {
            throw new JwtException("Token validation failed");
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && isTokenExpired(token));
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
        long expirationTime = 3600L * 1000; // 1 hour expiration time (or adjust as needed)
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject(); // or use Claims::getSubject directly
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtException, IllegalArgumentException  {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws JwtException, IllegalArgumentException {
            // Exception gets thrown here
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build().parseSignedClaims(token).getPayload();
    }

    @Override
    public String refreshToken(String token) {
        System.out.println("I GOT HERE");
        String tokenWithoutPrefix = token.replace("Bearer ", "").trim();

        if (isTokenExpired(tokenWithoutPrefix)) {
            throw new JwtException("Token has expired. Please login again.");
        }

        // Proceed with refreshing token since it’s not expired
        String emailOrUsername = this.extractEmailOrUsername(tokenWithoutPrefix);
        UserDetails userDetails = loadUserByEmailOrUsername(emailOrUsername);
        String sub = extractClaim(tokenWithoutPrefix, Claims::getSubject);
        String role = extractClaim(tokenWithoutPrefix, claims -> claims.get("role", String.class));

        // Create claims for the new token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("sub", sub);
        extraClaims.put("role", role);

        // Generate new token based on user role
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("CLIENT"))) {
            return this.getTokenWithClaims(extraClaims, userDetails);
        } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ADMIN"))) {
            return this.getTokenWithClaims(extraClaims, userDetails);
        } else {
            throw new LogicException("Unsupported user type");
        }
    }


    private String extractEmailOrUsername(String tokenWithoutPrefix) throws JSONException {
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


    private UserDetails loadUserByEmailOrUsername(String username) {
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
        Admin admin = adminRepository.findByUsername(username).orElse(null);

        if (admin != null) {
            return new org.springframework.security.core.userdetails.User(
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getAuthorities()
            );
        }
        // If user not found, throw exception
        throw new UsernameNotFoundException("User not found with username: " + username);
    }

}
