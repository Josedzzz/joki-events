package com.uq.jokievents.service.implementation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.uq.jokievents.dtos.*;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.model.enums.Role;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.utils.*;
import com.uq.jokievents.service.interfaces.JwtService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

//    @Override
//    public Map<Admin, String> loginAdmin(AuthAdminDTO request) {
//        String username = request.username();
//        String password = request.password();
//        Admin admin = adminRepository.findByUsername(username)
//                .orElseThrow(() -> new AccountException("Account not found in the database"));
//
//        if (!admin.isActive()) {
//            throw new AccountException("The admin is not active");
//        }
//
//        if (!passwordEncoder.matches(password, admin.getPassword())) {
//            throw new AccountException("Invalid username or password");
//        }
//
//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
//        Map<Admin, String> loginInfo = new HashMap<>();
//        String adminToken = jwtService.getAdminToken(admin);
//        loginInfo.put(admin, adminToken);
//        return loginInfo;
//    }

    @Override
    public Map<Admin, String> loginAdmin(AuthAdminDTO request) {
        String username = request.username();
        String password = request.password();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        if (authentication.isAuthenticated()) {
            Admin admin = (Admin) authentication.getPrincipal();
            String adminToken = jwtService.getAdminToken(admin);
            Map<Admin, String> loginInfo = new HashMap<>();
            loginInfo.put(admin, adminToken);
            return loginInfo;
        } else {
            throw new AccountException("Authentication failed");
        }
    }

//    @Override
//    public Map<Client, String> loginClient(LoginClientDTO request) {
//
//        String email = request.email();
//        String rawPassword = request.password();
//
//        Client client = clientRepository.findByEmail(email)
//                .orElseThrow(() -> new AccountException("Client not found"));
//
//        if (!client.isActive()) {
//            throw new AccountException("Client is not active");
//        }
//
//        if (!passwordEncoder.matches(rawPassword, client.getPassword())) {
//            throw new AccountException("Invalid password");
//        }
//
//        Map<Client, String> loginInfo = new HashMap<>();
//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
//        UserDetails clientDetails = clientRepository.findByEmail(email).orElse(null);
//        String token = jwtService.getClientToken(clientDetails);
//        loginInfo.put(client, token);
//        return loginInfo;
//    }

    @Override
    public Map<Client, String> loginClient(LoginClientDTO request) {

        String email = request.email();
        String rawPassword = request.password();
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(email, rawPassword);

        // Authenticate the token using AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        if (authentication.isAuthenticated()) {
            Client client = (Client) authentication.getPrincipal();
            if (!client.isActive()) throw new AccountException("Your account is not active"); // I could do this for admin too but whatever
            String clientToken = jwtService.getClientToken(client);
            Map<Client, String> loginInfo = new HashMap<>();
            loginInfo.put(client, clientToken);
            return loginInfo;
        } else {
            throw new AccountException("Authentication failed");
        }
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

    @Override
    public Map<String, String> registerUserIfNotExists(FirebaseToken decodedToken) {
        String email = decodedToken.getEmail();
        String name = decodedToken.getName();
        Client existingClient = clientRepository.findByEmail(email).orElse(null);

        if (existingClient != null) {
            // If client exists and is active, return the token immediately
            if (existingClient.isActive()) {
                String token = jwtService.getClientToken(existingClient);
                Map<String, String> registerInfo = new HashMap<>();
                registerInfo.put(existingClient.getId(), token);
                return registerInfo;
            } else {
                // Activate an existing but inactive client without requiring verification code
                existingClient.setActive(true);
                clientRepository.save(existingClient);
                String token = jwtService.getClientToken(existingClient);
                Map<String, String> registerInfo = new HashMap<>();
                registerInfo.put(existingClient.getId(), token);
                return registerInfo;
            }
        }
        return getLoginInfoFromClient(name, email);
    }

    private Map<String, String> getLoginInfoFromClient(String name, String email) {
        // New client creation
        Client client = Client.builder()
                .id(String.valueOf(new ObjectId()))
                .idCard("") // No ID card in Google sign-in, or generate a placeholder if required
                .name(name)
                .address("") // Address could be added later by the client, if needed
                .phoneNumber("") // No phone initially unless provided
                .email(email)
                .password(null) // No password set for Google sign-in clients, maybe insist? Now that I think of it, I don't have password in lots of sites
                .idShoppingCart(String.valueOf(new ObjectId()))
                .active(true) // Mark as active by default for Google accounts
                .listOfUsedCoupons(new ArrayList<>())
                .build();

        // Create shopping cart for the new Google client
        ShoppingCart clientShoppingCart = ShoppingCart.builder()
                .id(client.getIdShoppingCart())
                .clientId(client.getId())
                .paymentGatewayId("")
                .localityOrders(new ArrayList<>())
                .totalPrice(0.0)
                .totalPriceWithDiscount(0.0)
                .appliedDiscountPercent(1.0)
                .couponClaimed(false)
                .build();

        // Save client and shopping cart
        shoppingCartRepository.save(clientShoppingCart);
        clientRepository.save(client);

        // Send a welcome or discount email if desired
        emailService.sendDiscountCouponMail(client.getEmail());

        // Generate and return JWT token for new client
        String token = jwtService.getClientToken(client);
        Map<String, String> registerInfo = new HashMap<>();
        registerInfo.put(client.getId(), token);
        return registerInfo;
    }

    @Override
    public Map<String, String> googleLogin(String idToken) {
        // Verify the token with Firebase
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

            // Extract user details from the token
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();

            // Check if user exists
            Client client = clientRepository.findByEmail(email).orElse(null);

            if (client == null) {
                // New client creation, very rude but okay
                return getLoginInfoFromClient(name, email);
            }
            // If the user exists or has just been registered, generate a token for them
            String token = jwtService.getClientToken(client);
            // Return the client and their JWT token
            Map<String, String> loginInfo = new HashMap<>();
            loginInfo.put(client.getId(), token);
            return loginInfo;
        } catch (FirebaseAuthException e) {
            throw new LogicException(e.getMessage());
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
                .id(String.valueOf(new ObjectId()))
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
                .clientId(client.getId())
                .paymentGatewayId("")
                .localityOrders(new ArrayList<>())
                .totalPrice(0.0)
                .totalPriceWithDiscount(0.0)
                .appliedDiscountPercent(1.0)
                .couponClaimed(false)
                .build();

        // Save client and shopping cart
        shoppingCartRepository.save(clientShoppingCart);
        clientRepository.save(client);

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

