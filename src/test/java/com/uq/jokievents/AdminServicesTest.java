package com.uq.jokievents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uq.jokievents.config.ApplicationConfig;
import com.uq.jokievents.dtos.CreateCouponDTO;
import com.uq.jokievents.dtos.CreateLocalityDTO;
import com.uq.jokievents.dtos.HandleEventDTO;
import com.uq.jokievents.dtos.UpdateAdminDTO;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.enums.EventType;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.CouponRepository;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.service.implementation.JwtServiceImpl;
import com.uq.jokievents.utils.Generators;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private CouponRepository couponRepository;

    @Autowired
    private ApplicationConfig appConfig;

    // Simple globalAdmin for testing
    private Admin globalAdmin;
    @Autowired
    private EventRepository eventRepository;

    // SETUP FOR SOME METHODS THAT REQUIRE A PREVIOUS SIMULATION

    @BeforeEach
    public void setup() {
        // Set up a mock globalAdmin object
        setUpGlobalAdmin();

        // For COUPONS des GARÇONS
        // Insert a coupon with ID "670551394a7c0efcfe5fee77" into the database before each test for it to be deleted, the life of these bits is basically torture
        createTestCoupon();

        // FOR EVENTS
        createTestEvent();
    }


    @AfterEach
    public void tearDown() {
        // Clear the repository or remove the test admin if necessary
        adminRepository.delete(globalAdmin);
        SecurityContextHolder.clearContext(); // Clean up the security context
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

        // Two coupons having the same name is very unlikely
        String couponName = Generators.generateRndVerificationCode() + Generators.generateRndVerificationCode();

        // Create a DTO for the request
        CreateCouponDTO couponDTO = new CreateCouponDTO(couponName, 20.0, LocalDateTime.now().plusDays(30), 100.0);
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

    // Kind of weird test, it works but in a unique way.
    // It fails as intended (conflict between two names) but seems like the repository throws the error faster that the request gives its answer.
    // TODO Will look further when binding it with the frontend
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
        couponRepository.save(existingCoupon); // Save the coupon to the repository

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

    // UPDATE COUPON TESTING

    @Test
    public void testUpdateCoupon_Success() throws Exception {

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

        // Create a valid DTO for updating the coupon
        String requestBody = "{ \"discount\": 25.0, \"expirationDate\": \"2024-12-31T23:59:59\", \"minPurchaseAmount\": 150.0 }";

        // Perform the PUT request to update the coupon, coupon exists in the database
        mockMvc.perform(post("/api/admin/update-coupon/67054eb8734d84764f8b0316")
                        .header("Authorization", "Bearer "+currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Coupon updated"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    public void testUpdateCoupon_Unauthorized() throws Exception {
        // Create a valid DTO for updating the coupon
        String requestBody = "{ \"discount\": 25.0, \"expirationDate\": \"2024-12-31T23:59:59\", \"minPurchaseAmount\": 150.0 }";

        // Perform the PUT request without authentication
        mockMvc.perform(post("/api/admin/update-coupon/67054bc7ae5c63c8ce970714")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden()); // Expecting forbidden due to missing admin role and missing token
    }

    // TEST DELETE COUPON

    @Test
    public void testDeleteCoupon_Success() throws Exception {
        // Mock the security context to simulate an "ADMIN" user
        Authentication auth = new TestingAuthenticationToken("admin", null, Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(globalAdmin.getId()).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        // Perform the DELETE request for the coupon
        mockMvc.perform(delete("/api/admin/delete-coupon/670551394a7c0efcfe5fee77")
                        .header("Authorization", "Bearer " + currentToken))
                .andExpect(status().isOk()) // Expect 200 OK
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Coupon deleted"));

        // Verify that the coupon is no longer in the database
        Optional<Coupon> deletedCoupon = couponRepository.findById("670551394a7c0efcfe5fee77");
        assertFalse(deletedCoupon.isPresent(), "The coupon should be deleted from the database");
    }

    @Test
    public void testAddEvent_Success() throws Exception {

        // Mock the security context to simulate an "ADMIN" user
        Authentication auth = new TestingAuthenticationToken("admin", null, Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(globalAdmin.getId()).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        // Random string to add to the name
        String randomString = Generators.generateRndVerificationCode() + Generators.generateRndVerificationCode();

        // Prepare the HandleEventDTO for the test
        HandleEventDTO eventDTO = new HandleEventDTO(
                "Concert" + randomString,
                "New York",
                "Main St 123",
                LocalDateTime.now().plusDays(30), // Event date 30 days from now
                500,
                List.of(new CreateLocalityDTO("VIP", 100.0, 50)),
                appConfig.getBase64Image(), // Dummy image URL
                appConfig.getBase64Image(), // Dummy locality image URL
                EventType.CONCERT // Assuming enum EventType
        );
        // Perform the POST request with the event DTO in JSON format
        mockMvc.perform(post("/api/admin/create-event")
                        .header("Authorization", "Bearer "+currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventDTO))) // Convert DTO to JSON
                .andExpect(status().isCreated()) // Expect HTTP 201 CREATED
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Event created successfully"))
                .andExpect(jsonPath("$.data").exists()); // Check event data
    }

    // UPDATE EVENT SUCCESS TESTING

    @Test
    public void testUpdateEvent() throws Exception {
        String eventId = "6705590b39e3c64472be8665"; // Example event ID

        // Mock the security context to simulate an "ADMIN" user
        Authentication auth = new TestingAuthenticationToken("admin", null, Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(globalAdmin.getId()).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        String randomString = Generators.generateRndVerificationCode() + Generators.generateRndVerificationCode();

        HandleEventDTO updateDto = new HandleEventDTO(
                "Updated Event Name " + randomString,
                 "123 Updated Address",
                "Updated City",
                LocalDateTime.now().plusDays(1),
                100,
                List.of(new CreateLocalityDTO("ULTRA VIP" + randomString, 100.0, 50)),
                appConfig.getBase64Image(),
                appConfig.getBase64Image(),
                EventType.CONCERT
        ); // Create and set your DTO values

        // Perform the request to update the event
        mockMvc.perform(post("/api/admin/update-event/{eventId}", eventId) // Adjust the URL to your mapping
                        .header("Authorization", "Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Expect a 200 OK status
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Event updated")); // Adjust based on your ApiResponse structure
    }

    // IS IT EVEN NECESSARY TO TEST THE DELETION OF AN EVENT? COMMON MAN! THE CONSTRUCTION IS ALMOST PERFECT
    // QUE DICE NO SÉ INGLÉS
    // GRUPO SOY VANESA

    @Test
    public void testDeleteEvent() throws Exception {

        // Mock the security context to simulate an "ADMIN" user
        Authentication auth = new TestingAuthenticationToken("admin", null, Collections.singleton(new SimpleGrantedAuthority("ADMIN")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Simulate a valid JWT token for authorization
        UserDetails adminDetails = adminRepository.findById(globalAdmin.getId()).orElse(null);
        String currentToken = jwtService.getAdminToken(adminDetails);

        String eventId = "67055d410ca3ab8c7a42db3c";

        // Perform the request to delete the event
        mockMvc.perform(post("/api/admin/delete-event/{eventId}", eventId) // Adjust the URL to your mapping
                        .header("Authorization", "Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk()) // Expect a 200 OK status
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Event deleted")); // Adjust based on your ApiResponse structure
    }



    // AUXILIARY METHODS, This is not cheating. These instances will probable be deleted so inserting them in the dataset.js would be useless.

    private void createTestEvent() {
        // Create and save a test event with the specified ID
        Event event = new Event();
        event.setId("67055d410ca3ab8c7a42db3c");
        event.setName("Test Event");
        event.setAddress("123 Test Address");
        event.setCity("Test City");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setAvailableForPurchase(true);
        event.setTotalAvailablePlaces(100);

        // Save the event to the repository
        eventRepository.save(event);
    }

    private void createTestCoupon() {
        Coupon testCoupon = new Coupon();
        testCoupon.setId("670551394a7c0efcfe5fee77");
        testCoupon.setName("TestCoupon");
        testCoupon.setDiscountPercent(15.0);
        testCoupon.setExpirationDate(LocalDateTime.of(2025, 12, 31, 23, 59, 59));
        testCoupon.setMinPurchaseAmount(50.0);

        // Save the coupon to the database
        couponRepository.save(testCoupon);
    }

    private void setUpGlobalAdmin(){
        globalAdmin = new Admin();
        // Does not need id, this is just for jwt.
        globalAdmin.setEmail("existing@globalAdmin.com");
        globalAdmin.setUsername("saiko");
        globalAdmin.setVerificationCode("123456");
        globalAdmin.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15)); // Set a future expiration
        globalAdmin.setPassword("password");
        // Save globalAdmin to the repository
        adminRepository.save(globalAdmin);
    }
}


