package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.AuthAdminDTO;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    ResponseEntity<?> authenticate(AuthAdminDTO loginRequest);
    String refreshToken(String refreshToken);
}
