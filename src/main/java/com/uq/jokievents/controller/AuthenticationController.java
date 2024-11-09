package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    // redrum redrum redrum

    private final AuthenticationService authenticationService;

    @PostMapping("/login-admin")
    public ResponseEntity<ApiTokenResponse<String>> loginAdmin(@RequestBody @Valid AuthAdminDTO loginRequest) {
        try {
            // Call the service to attempt login and receive a map with Admin and JWT token
            Map<Admin, String> loginInfo = authenticationService.loginAdmin(loginRequest);

            // Get the only entry in the map, which is the logged-in Admin and their token
            Map.Entry<Admin, String> entry = loginInfo.entrySet().iterator().next();
            Admin admin = entry.getKey();
            String token = entry.getValue();

            ApiTokenResponse<String> response = new ApiTokenResponse<>(
                    "Success",
                    "Admin logged in successfully",
                    admin.getId(),
                    token
            );

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (AccountException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", "Login failed", null, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login-client")
    public ResponseEntity<ApiTokenResponse<String>> loginClient(@RequestBody @Valid LoginClientDTO loginRequest) {
        try {
            // Call the service to attempt login and receive a JWT token if successful
            Map<Client, String> loginInfo = authenticationService.loginClient(loginRequest);

            Map.Entry<Client, String> entry = loginInfo.entrySet().iterator().next();
            Client client = entry.getKey();
            String token = entry.getValue();

            // Construct a successful response with the token
            ApiTokenResponse<String> response = new ApiTokenResponse<>(
                    "Success",
                    "Client logged in successfully",
                    client.getId(),
                    token
            );
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (AccountException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        } catch (Exception e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", "Login failed", null, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/register-client")
    public ResponseEntity<ApiTokenResponse<String>> registerClient(@RequestBody @Valid RegisterClientDTO request) {
        try {
            // Call the service method
            Map<Client, String> registerInfo = authenticationService.registerClient(request);

            Map.Entry<Client, String> entry = registerInfo.entrySet().iterator().next();
            Client client = entry.getKey();
            String token = entry.getValue();

            // Construct a successful response
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Success", "Client registered successfully", client.getId(), token);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (AccountException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", "Client registration failed", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // EmailDTO the goat for real!
    @PostMapping("/send-recover-password-code")
    public ResponseEntity<ApiResponse<String>> sendRecoverPasswordCode(@RequestBody @Valid EmailDTO dto) {
        try {
            // Call the service method and get the success message if possible
            String resultMessage = authenticationService.sendRecoverPasswordCode(dto);
            ApiResponse<String> response = new ApiResponse<>("Success", resultMessage, null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Password code sending failed", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/recover-password")
    public ResponseEntity<ApiResponse<String>> recoverPassword(@RequestBody @Valid RecoverPassDTO dto) {
        try {
            authenticationService.recoverPassword(dto);

            ApiResponse<String> response = new ApiResponse<>("Success", "Password recovery completed successfully", null);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (AccountException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Password recovery failed", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

