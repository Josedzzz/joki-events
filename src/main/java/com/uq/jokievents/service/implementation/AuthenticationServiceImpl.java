package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.utils.*;
import com.uq.jokievents.service.interfaces.JwtService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
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
    private final ShoppingCartRepository shoppingCartRepository;
    private final Utils utils;
    private final EmailService emailService;

    @Override
    public ResponseEntity<?> loginAdmin(AuthAdminDTO request) {
        try {
            String username = request.username();
            String password = request.password();
            Optional<Admin> adminDB = adminRepository.findByUsername(username);
            if (adminDB.isPresent()) {
                if (adminDB.get().isActive()) {
                     if (passwordEncoder.matches(password, adminDB.get().getPassword())) {
                        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
                        UserDetails adminDetails = adminRepository.findByUsername(username).orElse(null);
                        String token = jwtService.getAdminToken(adminDetails);
                        ApiTokenResponse<String> response = new ApiTokenResponse<>("Success", "Admin logged in successfully", adminDB.get().getId(), token);
                        return new ResponseEntity<>(response, HttpStatus.CREATED);
                     } else {
                         ApiResponse<String> response = new ApiResponse<>("Error", "Invalid username or password", null);
                         return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                     }
                }
                else{
                    ApiResponse<String> response = new ApiResponse<>("Error", "The admin is not active", null);
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Account not found in database", null);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
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
    public ResponseEntity<?> sendRecoverPasswordCode(EmailDTO dto) {
        try {
            String email = dto.email(); // Je me sens tellement dérangé

            if (adminRepository.findByEmail(email).isPresent()) {
                Admin admin = adminRepository.findByEmail(email).get();
                // Generate a new verification code
                String verificationCode = Generators.generateRndVerificationCode();

                // Set the expiration time to 20 minutes from now
                admin.setVerificationCode(verificationCode);
                admin.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(20));

                // Save the updated admin with the verification code and expiration time
                adminRepository.save(admin);

                // Send the recovery email
                emailService.sendRecuperationEmail(admin.getEmail(), verificationCode);
                ApiResponse<String> response = new ApiResponse<>("Success", "Recovery code sent", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else if(clientRepository.findByEmail(email).isPresent()) {
                Client client = clientRepository.findByEmail(email).get();

                // Generate a new verification code
                String verificationCode = Generators.generateRndVerificationCode();

                // Set the expiration time to 20 minutes from now
                client.setVerificationCode(verificationCode);
                client.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(20));

                // Save the updated admin with the verification code and expiration time
                clientRepository.save(client);

                // Send the recovery email
                emailService.sendRecuperationEmail(client.getEmail(), verificationCode);
                ApiResponse<String> response = new ApiResponse<>("Success", "Recovery code sent", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "No one is registered with that email", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Password code sending failed", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> recoverPassword(RecoverPassDTO dto) {
        //est-ce informatiquement inefficace ? Cette méthode sera-t-elle vraiment là ? Devons-nous gérer un lien ?
        String email = dto.email();
        String verificationCode =  dto.verificationCode();
        String newPassword = passwordEncoder.encode(dto.newPassword());

        try {
            if (adminRepository.findByEmail(email).isPresent()) {
                Admin admin = adminRepository.findByEmail(email).orElse(null);
                assert admin != null;

                if (admin.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "Verification code has expired", null);
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }

                // Verify if the code matches (assuming the admin entity has a verification code field) (Jose will make sure of it)
                if (!admin.getVerificationCode().equals(verificationCode)) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "Invalid verification code", null);
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }

                // Update the password
                admin.setPassword(newPassword);
                admin.setVerificationCode("");
                adminRepository.save(admin);

                ApiResponse<String> response = new ApiResponse<>("Success", "Password recovery done", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if (clientRepository.findByEmail(email).isPresent()){
                Client client = clientRepository.findByEmail(email).orElse(null);
                assert client != null;

                if (client.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "Verification code has expired", null);
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }

                // Verify if the code matches (assuming the admin entity has a verification code field) (Jose will make sure of it)
                if (!client.getVerificationCode().equals(verificationCode)) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "Invalid verification code", null);
                    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
                }

                // Update the password
                client.setPassword(newPassword);
                client.setVerificationCode("");
                clientRepository.save(client);

                ApiResponse<String> response = new ApiResponse<>("Success", "Password recovery done", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "No one is registered with that email", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Password recovery failed", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> registerClient(@Valid RegisterClientDTO request) {
        // Usando el builder para crear el cliente
        Client client = Client.builder()
                .idCard(request.idCard())
                .name(request.name())
                .address(request.address())
                .phoneNumber(request.phone())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
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
        client.setListOfUsedCoupons(new ArrayList<>());
        // Creating the ShoppingCart instance unique to the client
        client.setIdShoppingCart(String.valueOf(new ObjectId()));
        ShoppingCart clientShoppingCart = ShoppingCart.builder()
                .id(client.getIdShoppingCart())
                .paymentGatewayId("")
                .idClient(client.getId())
                .localityOrders(new ArrayList<>())
                .totalPrice(0.0)
                .totalPriceWithDiscount(0.0)
                .orderPayment(null)
                .paymentCoupon("")
                .appliedDiscountPercent(1.0)
                .couponClaimed(false)
                .build();

        client.setActive(false);

        // Sending the verification email
        emailService.sendVerificationMail(client.getEmail(), verificationCode);

        // Stores the client and its shopping cart in the database
        clientRepository.save(client);
        shoppingCartRepository.save(clientShoppingCart);

        // Returns a response entity
        ApiTokenResponse<Object> response = new ApiTokenResponse<>("Success", "Client registered successfully", client.getId(), jwtService.getClientToken(client));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}

