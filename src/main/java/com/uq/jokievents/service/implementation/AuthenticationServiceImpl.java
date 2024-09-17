package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public ResponseEntity<?> authenticate(AuthAdminDTO loginRequest) {
        // Authenticate the user based on the provided credentials, will all the try catch later.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );
        // Generate a JWT token for the authenticated user

        ApiResponse<String> response = new ApiResponse<>("Success", "Admin authenticated", jwtUtil.generateToken(authentication));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public String refreshToken(String refreshToken) {
        // Logic for refreshing the JWT token, will add the try-catch later
        return jwtUtil.refreshToken(refreshToken);
    }
}

