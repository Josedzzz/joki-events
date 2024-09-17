package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

// TODO This should be like an interface or directly go in the Controller classes.
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {


    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> loginAdmin(@RequestBody @Valid AuthAdminDTO loginRequest) {
        return authenticationService.authenticate(loginRequest);
    }
// This method is to refresh a token after certain requirements
//    @PostMapping("/refresh-token")
//    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
//        String newToken = authenticationService.refreshToken(request);
//        return ResponseEntity.ok(new AuthResponse(newToken));
//    }
}

