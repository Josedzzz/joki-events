package com.uq.jokievents.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.paypal.orders.*;
import com.uq.jokievents.dtos.*;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.LocalityOrder;
import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.service.interfaces.JwtService;
import com.uq.jokievents.service.interfaces.PaymentService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    // redrum redrum redrum

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final PaymentService paymentService;
    private final ShoppingCartRepository shoppingCartRepository;

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
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
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
        } catch (LogicException e) {
            // todo tell Jose about this possibility when registering
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Redirect", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
        catch (Exception e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(),null, null);
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
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
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
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiTokenResponse<?>> refreshJwtToken(@RequestHeader("Authorization") String token) {
        try {
            String newToken = jwtService.refreshToken(token);
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Success", "Returning new token", null, newToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (LogicException e) {
            // Return a friendly error response to Postman if the token is expired or invalid
            ApiTokenResponse<?> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            // General error response for unexpected exceptions
            ApiTokenResponse<?> response = new ApiTokenResponse<>("Error", "An unexpected error occurred.", null, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/google-sign-in")
    public ResponseEntity<ApiTokenResponse<?>> googleSignIn(@RequestParam String idToken) {
        try {
            // Verify the token with Firebase
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // Register the user if not exists
            Map<String, String> newClient = authenticationService.registerUserIfNotExists(decodedToken);
            Map.Entry<String, String> entry = newClient.entrySet().iterator().next();
            String clientId = entry.getKey();
            String token = entry.getValue();

            // Return a response
            ApiTokenResponse<String> response = new ApiTokenResponse<>("success", "User authenticated and registered successfully.", clientId, token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (FirebaseAuthException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/google-login")
    public ResponseEntity<ApiTokenResponse<String>> loginWithGoogle(@RequestBody GoogleUserDTO request) {

        try {
            String idToken = request.idToken();
            Map<String, String> loginInfo = authenticationService.googleLogin(idToken);
            Map.Entry<String, String> entry = loginInfo.entrySet().iterator().next();
            String clientId = entry.getKey();
            String newToken = entry.getValue();
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Success", "Google login successful", clientId, newToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/payment-success")
    public ResponseEntity<ApiResponse<String>> handlePaymentSuccess(@RequestParam("token") String token) {
        try {
            // Step 1: Capture the payment
            Capture capture = paymentService.capturePayment(token);

            // Step 2: Check if payment status is 'COMPLETED'
            if ("COMPLETED".equals(capture.status())) {
                // Step 3: Retrieve the ShoppingCart based on the payment token
                Optional<ShoppingCart> order = shoppingCartRepository.findByPaymentGatewayId(token);
                if (order.isEmpty()) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "Could not find the order", capture.status());
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }
                paymentService.updateEventAndLocalities(order.get());
                paymentService.fillPurchaseAfterSuccess(order.get());
                ApiResponse<String> response = new ApiResponse<>("Success", "Payment done", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Success", "Payment done, probably cancelled", capture.status());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Qué pesar este método kjaskjaksjkajs
    @GetMapping("/payment-cancel")
    public ResponseEntity<ApiResponse<String>> handlePaymentCancel() {
        ApiResponse<String> response = new ApiResponse<>("Success", "Payment cancelled", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

