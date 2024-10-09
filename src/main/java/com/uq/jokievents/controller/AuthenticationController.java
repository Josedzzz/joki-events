package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.dtos.RegisterClientDTO;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody @Valid AuthAdminDTO loginRequest) {
        return authenticationService.loginAdmin(loginRequest);
    }

    @PostMapping("/login-client")
    public ResponseEntity<?> loginClient(@RequestBody @Valid LoginClientDTO loginClientRequest) {
        return authenticationService.loginClient(loginClientRequest);
    }

    @PostMapping("/register-client")
    public ResponseEntity<?> registerClient(@RequestBody @Valid RegisterClientDTO registerClientRequest) {
        return authenticationService.registerClient(registerClientRequest);
    }
}

