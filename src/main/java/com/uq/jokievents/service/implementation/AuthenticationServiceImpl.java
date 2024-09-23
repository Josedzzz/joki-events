package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.dtos.RegisterClientDTO;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.enums.Role;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.utils.*;
import com.uq.jokievents.service.interfaces.JwtService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final ClientRepository clientRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AdminRepository adminRepository;

    @Autowired
    private Utils utils;
    @Autowired
    private EmailService emailService;

    @Override
    public ResponseEntity<?> loginAdmin(@Valid AuthAdminDTO request) {
        try {
            String username = request.username();
            String password = request.password();
            Optional<Admin> adminDB = adminRepository.findByUsername(username);
            if (adminDB.isPresent() && adminDB.get().isActive()) {
                if (passwordEncoder.matches(password, adminDB.get().getPassword())) {
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
                    UserDetails adminDetails = adminRepository.findByUsername(username).orElse(null);
                    String token = jwtService.getAdminToken(adminDetails);
                    ApiTokenResponse<String> response = new ApiTokenResponse<>("Success", "Admin logged in successfully", adminDB.get().getId(), token);
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                }
                else{
                    ApiResponse<String> response = new ApiResponse<>("Error", "The admin is not active", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Invalid username or password", null);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Login failed", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> loginClient(@Valid LoginClientDTO request) {
        try {
            String email = request.email();
            String rawPassword = request.password();
            Optional<Client> clientDB = clientRepository.findByEmail(email);
            if (clientDB.isPresent() && clientDB.get().isActive()) {
                if(passwordEncoder.matches(rawPassword, clientDB.get().getPassword())) {
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
                    UserDetails clientDetails = clientRepository.findByEmail(email).orElse(null);
                    String token = jwtService.getClientToken(clientDetails);
                    ApiTokenResponse<String> response = new ApiTokenResponse<>("Success", "Client logged in successfully", clientDB.get().getId(), token);
                    return new ResponseEntity<>(response, HttpStatus.CREATED);
                }
                else{
                    ApiResponse<String> response = new ApiResponse<>("Error", "The client is not active", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Invalid email or password", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to find client", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> registerClient(@Valid RegisterClientDTO request) {
        // Usando el builder para crear el cliente
        Client client = Client.builder()
                .idCard(request.idCard())
                .name(request.name())
                .direction(request.address())
                .phoneNumber(request.phone())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CLIENT)
                .build();

        // Few verifications for client registration
        if (utils.existsByIdCard(client.getIdCard())) {
            ApiResponse<String> response = new ApiResponse<>("Error", "The identification card is in use", null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (utils.existsEmailClient(client.getEmail())) {
            ApiResponse<String> response = new ApiResponse<>("Error", "The email is in use", null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Generating a verification code along with its expiration date
        String verificationCode = Generators.generateRndVerificationCode();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);

        // Assigning other attributes
        client.setVerificationCode(verificationCode);
        client.setVerificationCodeExpiration(expiration);
        client.setIdCoupons(new ArrayList<>());
        client.setIdShoppingCart(new ObjectId());
        client.setActive(false);

        // Sending the verification email
        emailService.sendVerificationMail(client.getEmail(), verificationCode);

        // Stores the client in the database
        clientRepository.save(client);

        // Returns a response entity
        ApiTokenResponse<Object> response = new ApiTokenResponse<>("Success", "Client registered successfully", client.getId(), jwtService.getClientToken(client));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}

