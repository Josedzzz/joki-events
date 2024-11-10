package com.uq.jokievents.service.interfaces;
import io.jsonwebtoken.*;
import org.json.JSONException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.function.Function;
// Hayao Matsumura
// todo see how to refresh a token
public interface JwtService {
    Date extractExpiration(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String getClientToken(UserDetails client);
    String getUsernameFromToken(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    String getAdminToken(UserDetails admin);
    String refreshToken(String token) throws JSONException;
}
