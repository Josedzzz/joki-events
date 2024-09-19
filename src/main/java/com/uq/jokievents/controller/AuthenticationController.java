package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.dtos.RegisterClientDTO;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.AuthResponse;
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

    @PostMapping("/login-admin")
    public ResponseEntity<?> loginAdmin(@RequestBody @Valid AuthAdminDTO loginRequest) {
        return ResponseEntity.ok(authenticationService.login(loginRequest));
        // return authenticationService.authenticate(loginRequest);
    }

    @PostMapping("/login-client")
    public ResponseEntity<?> loginClient(@RequestBody @Valid LoginClientDTO loginClientRequest) {
        return ResponseEntity.ok(authenticationService.loginClient(loginClientRequest));
    }

    @PostMapping("/register-client")
    public ResponseEntity<?> registerClient(@RequestBody @Valid RegisterClientDTO registerClientRequest) {
        return ResponseEntity.ok(authenticationService.registerClient(registerClientRequest));
    }
}
