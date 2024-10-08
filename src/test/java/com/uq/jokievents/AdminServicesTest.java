package com.uq.jokievents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uq.jokievents.dtos.CreateCouponDTO;
import com.uq.jokievents.dtos.UpdateAdminDTO;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.CouponRepository;
import com.uq.jokievents.service.interfaces.CouponService;
import com.uq.jokievents.service.implementation.JwtServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AdminServicesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private JwtServiceImpl jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    // Simple globalAdmin for testing
    private Admin globalAdmin;

    // SETUP FOR SOME METHODS THAT REQUIRE A PREVIOUS SIMULATION

    @BeforeEach
    public void setup() {
        // Set up a mock globalAdmin object
        globalAdmin = new Admin();
        globalAdmin.setEmail("existing@globalAdmin.com");
        globalAdmin.setUsername("saiko");
        globalAdmin.setVerificationCode("123456");
        globalAdmin.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15)); // Set a future expiration
        globalAdmin.setPassword("password");

        // Save globalAdmin to the repository
        adminRepository.save(globalAdmin);

        // For COUPONS des GARÇONS
        couponRepository.deleteAll(); // Ensure you have access to the coupon repository
    }


    @AfterEach
    public void tearDown() {
        // Clear the repository or remove the test admin if necessary
        adminRepository.delete(globalAdmin);
        SecurityContextHolder.clearContext(); // Clean up the security context
        couponRepository.deleteAll(); // Delete all Coupons again
    }


    // UPDATING ADMIN METHOD TESTING

    @Test
    public void testUpdateAdmin_Success() throws Exception {
        // The valid adminId for the request
        String adminId = "66f3aeb160c236c93c22b808"; // This is in the dataset.js file.

        // Create the request body (UpdateAdminDTO)
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO("balineroo", "balinius@gmail.com");

        // Simulate a valid JWT token (you could mock it using JwtService if needed)
        UserDetails adminDetails = adminRepository.findById(adminId).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        // Perform the PUT request using MockMvc
        mockMvc.perform(post("/api/admin/66f3aeb160c236c93c22b808/update/")
                        .header("Authorization","Bearer " + currentToken)  // Assuming you're passing a token
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAdminDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
    }

    @Test
    public void testUpdateAdmin_EmptyFields() throws Exception {
        // The valid adminId for the request
        String adminId = "66f3aeb160c236c93c22b808"; // This ID exists in the dataset

        // Create the request body with empty fields
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO(null, null);

        // Simulate a valid JWT token
        UserDetails adminDetails = adminRepository.findById(adminId).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        // Perform the PUT request using MockMvc
        mockMvc.perform(post("/api/admin/" + adminId + "/update/")
                        .header("Authorization", "Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAdminDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("Validation Failed")) // Assuming validation error is handled this way
                .andExpect(jsonPath("$.message").exists()); // Expecting a validation error message
    }

    // DELETED (ACCOUNT DEACTIVATED) ADMIN METHOD TESTING

    @Test
    public void testDeleteAdminAccount_Success() throws Exception {
        // The valid adminId for the request
        String adminId = "66f3aeb160c236c93c22b808"; // Assuming this globalAdmin exists in the dataset

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(adminId).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        // Perform the DELETE request using MockMvc
        mockMvc.perform(delete("/api/admin/" + adminId + "/delete")
                        .header("Authorization", "Bearer " + currentToken))
                .andDo(print())
                .andExpect(status().isOk()) // Expecting 200 OK
                .andExpect(jsonPath("$.status").value("Success")) // Expecting success message
                .andExpect(jsonPath("$.message").value("Admin account deactivated"));
    }

    //  SEND RECOVER PASSWORD TESTING
    // Only testing success here as recreating the security context is hard with an incorrect email, also, other than success in this method is unlikely
    @Test
    public void testSendRecoverPasswordCode_Success() throws Exception {

        // Existing globalAdmin email
        String validEmail = "balinius11@gmail.com";
        Admin adminAux = new Admin(); // mémoire

        Optional<Admin> adminOptional = adminRepository.findByEmail(validEmail);
        if (adminOptional.isPresent()) {
            adminAux = adminOptional.get();
        }

        // Mock the security context to simulate an "ADMIN" user
        Set<SimpleGrantedAuthority> simpleGrantedAuthorities = new java.util.HashSet<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority("ADMIN"));
        Authentication auth = new TestingAuthenticationToken("admin", null, simpleGrantedAuthorities);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(adminAux.getId()).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        System.out.println("GENERATED TOKEN: " + currentToken);

        mockMvc.perform(post("/api/admin/send-recover-code")
                        .header("Authorization", "Bearer " + currentToken)
                        .param("email", validEmail)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Recovery code sent"));

        // Clean up the security context after the test
        SecurityContextHolder.clearContext();
    }


    @Test
    public void testRecoverPassword_Success() throws Exception {
        // Use the valid email from the setup
        String validEmail = globalAdmin.getEmail();

        // Mock the security context to simulate an "ADMIN" user
        Authentication auth = new TestingAuthenticationToken("admin", null, Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findByEmail(validEmail).orElse(null);
        if (adminDetails != null) {
            String currentToken = jwtService.getAdminToken(adminDetails);
            System.out.println("GENERATED TOKEN: " + currentToken);

            // Create a DTO for the request
            String requestBody = "{ \"email\": \"" + validEmail + "\", \"verificationCode\": \"123456\", \"newPassword\": \"newSecurePassword\" }";

            // Perform the POST request
            mockMvc.perform(post("/api/admin/recover-password/")
                            .header("Authorization", "Bearer " + currentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("Success"))
                    .andExpect(jsonPath("$.message").value("Password recovery done"));

            // Verify if the password was updated
            Optional<Admin> updatedAdminOptional = adminRepository.findByEmail(validEmail);
            assertTrue(updatedAdminOptional.isPresent());
            Admin updatedAdmin = updatedAdminOptional.get();
            // Use your password encoding logic here to verify the password
            assertTrue(passwordEncoder.matches("newSecurePassword", updatedAdmin.getPassword()));
        } else {
            fail("Admin not found in the database.");
        }
    }

    // CREATE COUPON TESTING

    @Test
    public void testCreateCoupon_Success() throws Exception {
        // Existing globalAdmin email (globalAdmin exists in DB)
        String validEmail = "balinius11@gmail.com";
        Admin adminAux = new Admin(); // mémoire
        Optional<Admin> adminOptional = adminRepository.findByEmail(validEmail);
        if (adminOptional.isPresent()) {
            adminAux = adminOptional.get();
        }

        // Mock the security context to simulate an "ADMIN" user
        Authentication auth = new TestingAuthenticationToken("admin", null, Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(adminAux.getId()).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        // Create a DTO for the request
        CreateCouponDTO couponDTO = new CreateCouponDTO("SummerSale", 20.0, LocalDateTime.now().plusDays(30), 100.0);
        String requestBody = "{ \"name\": \"" + couponDTO.name() + "\", \"discount\": " + couponDTO.discount() + ", \"expirationDate\": \"" + couponDTO.expirationDate() + "\", \"minPurchaseAmount\": " + couponDTO.minPurchaseAmount() + " }";

        // Perform the POST request
        mockMvc.perform(post("/api/admin/create-coupon")
                        .header("Authorization", "Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Coupon creation done"))
                .andExpect(jsonPath("$.data.name").value(couponDTO.name()))
                .andExpect(jsonPath("$.data.discountPercent").value(couponDTO.discount()))
                .andExpect(jsonPath("$.data.minPurchaseAmount").value(couponDTO.minPurchaseAmount()));
    }

    @Test
    public void testCreateCoupon_Conflict() throws Exception {
        // Existing globalAdmin email (globalAdmin exists in DB)
        String validEmail = "balinius11@gmail.com";
        Admin adminAux = new Admin(); // mémoire
        Optional<Admin> adminOptional = adminRepository.findByEmail(validEmail);
        if (adminOptional.isPresent()) {
            adminAux = adminOptional.get();
        }

        // Mock the security context to simulate an "ADMIN" user
        Authentication auth = new TestingAuthenticationToken("admin", null, Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(adminAux.getId()).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        // Create and save a coupon to simulate a conflict
        Coupon existingCoupon = new Coupon();
        existingCoupon.setName("SummerSale");
        existingCoupon.setDiscountPercent(20.0);
        existingCoupon.setExpirationDate(LocalDateTime.now().plusDays(30));
        existingCoupon.setMinPurchaseAmount(100.0);
        couponService.saveCoupon(existingCoupon); // Save the coupon to the repository

        // Create a DTO for the request that conflicts with the existing coupon
        CreateCouponDTO couponDTO = new CreateCouponDTO("SummerSale", 25.0, LocalDateTime.now().plusDays(30), 200.0);
        String requestBody = "{ \"name\": \"" + couponDTO.name() + "\", \"discount\": " + couponDTO.discount() + ", \"expirationDate\": \"" + couponDTO.expirationDate() + "\", \"minPurchaseAmount\": " + couponDTO.minPurchaseAmount() + " }";

        // Perform the POST request
        mockMvc.perform(post("/api/admin/create-coupon")
                        .header("Authorization", "Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("Error"))
                .andExpect(jsonPath("$.message").value("Coupon with the same name already exists"));
    }
}


