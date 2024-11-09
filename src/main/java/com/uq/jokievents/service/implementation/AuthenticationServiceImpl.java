package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.exceptions.*;
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

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    private final EmailService emailService;

    @Override
    public Map<Admin, String> loginAdmin(AuthAdminDTO request) {
        String username = request.username();
        String password = request.password();
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new AccountException("Account not found in the database"));

        if (!admin.isActive()) {
            throw new AccountException("The admin is not active");
        }

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new AccountException("Invalid username or password");
        }

        // Exception throws here
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        Map<Admin, String> loginInfo = new HashMap<>();
        String adminToken = jwtService.getAdminToken(admin);
        loginInfo.put(admin, adminToken);
        return loginInfo;
    }

    @Override
    public Map<Client, String> loginClient(LoginClientDTO request) {

        String email = request.email();
        String rawPassword = request.password();

        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new AccountException("Client not found"));

        if (!client.isActive()) {
            throw new AccountException("Client is not active");
        }

        if (!passwordEncoder.matches(rawPassword, client.getPassword())) {
            throw new AccountException("Invalid password");
        }

        Map<Client, String> loginInfo = new HashMap<>();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
        UserDetails clientDetails = clientRepository.findByEmail(email).orElse(null);
        String token = jwtService.getClientToken(clientDetails);
        loginInfo.put(client, token);
        return loginInfo;
    }

    @Override
    public String sendRecoverPasswordCode(EmailDTO dto) {
        String email = dto.email();

        // Check if the email belongs to an Admin or Client and process accordingly
        if (adminRepository.findByEmail(email).isPresent()) {
            Admin admin = adminRepository.findByEmail(email).orElseThrow(() -> new AccountException("Admin not found"));
            sendVerificationCodeAdmin(admin);
            return "Recovery code sent to Admin successfully";
        } else if (clientRepository.findByEmail(email).isPresent()) {
            Client client = clientRepository.findByEmail(email).orElseThrow(() -> new AccountException("Client not found"));
            sendVerificationCodeClient(client);
            return "Recovery code sent to Client successfully";
        } else {
            throw new AccountException("No one is registered with that email");
        }
    }

    private void sendVerificationCodeClient(Client client) {
        String verificationCode = Generators.generateRndVerificationCode();

        client.setVerificationCode(verificationCode);
        client.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(20));

        clientRepository.save(client);

        emailService.sendRecuperationEmail(client.getEmail(), verificationCode);
    }

    private void sendVerificationCodeAdmin(Admin admin) {
        String verificationCode = Generators.generateRndVerificationCode();

        // Set the expiration time to 20 minutes from the server hour
        admin.setVerificationCode(verificationCode);
        admin.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(20));

        adminRepository.save(admin);

        // Send the recovery email
        emailService.sendRecuperationEmail(admin.getEmail(), verificationCode);
    }

    @Override
    public void recoverPassword(RecoverPassDTO dto) {
        String email = dto.email();
        String verificationCode = dto.verificationCode();
        String newPassword = passwordEncoder.encode(dto.newPassword());

        // Attempt to recover password for either Admin or Client
        if (adminRepository.findByEmail(email).isPresent()) {
            Admin admin = adminRepository.findByEmail(email).orElseThrow(() -> new AccountException("Admin not found"));

            validateVerificationCode(admin.getVerificationCode(), admin.getVerificationCodeExpiration(), verificationCode);

            updateAdminPassword(admin, newPassword);
            adminRepository.save(admin);
        } else if (clientRepository.findByEmail(email).isPresent()) {
            Client client = clientRepository.findByEmail(email).orElseThrow(() -> new AccountException("Client not found"));

            validateVerificationCode(client.getVerificationCode(), client.getVerificationCodeExpiration(), verificationCode);

            updateClientPassword(client, newPassword);
            clientRepository.save(client);
        } else {
            throw new AccountException("No account found with that email");
        }
    }

    private void validateVerificationCode(String storedCode, LocalDateTime expiration, String providedCode) {
        if (expiration.isBefore(LocalDateTime.now())) {
            throw new LogicException("Verification code has expired");
        }

        if (!storedCode.equals(providedCode)) {
            throw new LogicException("Invalid verification code");
        }
    }

    private void updateClientPassword(Client client, String newPassword) {
        client.setPassword(newPassword);
        client.setVerificationCode("");
    }

    private void updateAdminPassword(Admin admin, String newPassword) {
        admin.setPassword(newPassword);
        admin.setVerificationCode("");
    }


    @Override
    public Map<Client, String> registerClient(@Valid RegisterClientDTO request) {
        // this method is "a prueba de bombas"
        String email = request.email();
        Client slowClient = clientRepository.findByEmail(email).orElse(null);

        // Check if the client exists already
        if (slowClient != null) {
            // If the client is active, throw an error
            if (slowClient.isActive()) {
                throw new AccountException("Client is already active");
            } else {
                // Resend verification code for inactive clients
                String newVerificationCode = Generators.generateRndVerificationCode();
                slowClient.setVerificationCode(newVerificationCode);
                slowClient.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15));
                clientRepository.save(slowClient);
                emailService.sendVerificationMail(email, newVerificationCode);
                throw new LogicException("Could not activate your account last time; verification code resent to your email.");
            }
        }

        // Check for unique ID card constraint
        if (clientRepository.existsByIdCard(request.idCard())) {
            throw new AccountException("The identification card is in use");
        }

        // Client creation
        Client client = Client.builder()
                .idCard(request.idCard())
                .name(request.name())
                .address(request.address())
                .phoneNumber(request.phone())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        // Generate and assign verification code with expiration
        String verificationCode = Generators.generateRndVerificationCode();
        client.setVerificationCode(verificationCode);
        client.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15));
        client.setListOfUsedCoupons(new ArrayList<>());
        client.setIdShoppingCart(String.valueOf(new ObjectId()));
        client.setActive(false); // Set as inactive until verified

        // Create shopping cart for client
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

        // Save client and shopping cart
        clientRepository.save(client);
        shoppingCartRepository.save(clientShoppingCart);

        // Send verification and discount emails after successful save
        emailService.sendVerificationMail(client.getEmail(), verificationCode);
        emailService.sendDiscountCouponMail(client.getEmail());

        // Prepare registration information for response
        Map<Client, String> registerInfo = new HashMap<>();
        String token = jwtService.getClientToken(client);
        registerInfo.put(client, token);
        return registerInfo;
    }


}

