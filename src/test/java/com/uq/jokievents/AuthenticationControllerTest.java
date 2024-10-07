package com.uq.jokievents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.dtos.LoginClientDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Test successful login for an admin
    @Test
    public void testLoginAdmin_Success() throws Exception {
        // Create a valid AuthAdminDTO object for the test
        AuthAdminDTO loginRequest = new AuthAdminDTO("balin", "1234");

        mockMvc.perform(post("/auth/login-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isCreated()) // Expecting a 201 Created response
                .andExpect(jsonPath("$.status").value("Success")) // No casting needed
                .andExpect(jsonPath("$.message").value("Admin logged in successfully"))
                .andExpect(jsonPath("$.token").exists()); // Check for token existence
    }


    // Test login failure when the admin provides invalid credentials
    @Test
    public void testLoginAdmin_InvalidCredentials() throws Exception {
        // Create an AuthAdminDTO object with invalid credentials
        AuthAdminDTO loginRequest = new AuthAdminDTO("balinero", "1111");

        // Perform the POST request to the /login-admin endpoint
        mockMvc.perform(post("/auth/login-admin")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // Optional: Prints the request/response details
                .andExpect(status().isUnauthorized()) // Expecting a 401 UNAUTHORIZED status
                .andExpect((ResultMatcher)jsonPath("$.status").value("Error"))
                .andExpect((ResultMatcher)jsonPath("$.message").exists());
    }

    // Test login failure when the admin is not active
    @Test
    public void testLoginAdmin_InactiveAdmin() throws Exception {
        // Assuming this username belongs to an inactive admin
        AuthAdminDTO loginRequest = new AuthAdminDTO("balinero", "1234");

        mockMvc.perform(post("/auth/login-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized()) // Expecting a 400 Bad Request for inactive admins
                .andExpect(jsonPath("$.status").value("Error"))
                .andExpect(jsonPath("$.message").value("The admin is not active"));
    }


    // Test validation failure for missing fields in the request
    @Test
    public void testLoginAdmin_ValidationFailure() throws Exception {
        // Create an AuthAdminDTO object with invalid fields (e.g., missing username)
        AuthAdminDTO loginRequest = new AuthAdminDTO("c", "c");

        // Perform the POST request to the /login-admin endpoint
        mockMvc.perform(post("/auth/login-admin")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // Optional: Prints the request/response details
                .andExpect(status().isBadRequest()) // Expecting a 400 BAD REQUEST status
                .andExpect((ResultMatcher)jsonPath("$.status").value("Validation Failed")) // Assuming validation error is handled this way
                .andExpect((ResultMatcher)jsonPath("$.message").exists()); // Expecting a validation error message
    }

    // --- CLIENTS

    @Test
    public void testLoginClient_Success() throws Exception {
        // Valid credentials for an active client
        LoginClientDTO loginRequest = new LoginClientDTO("balinius11@gmail.com", "1234");

        mockMvc.perform(post("/auth/login-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isCreated()) // Expecting a 201 Created for successful login
                .andExpect(jsonPath("$.status").value("Success"))
                .andExpect(jsonPath("$.message").value("Client logged in successfully"));
    }


    @Test
    public void testLoginClient_InvalidCredentials() throws Exception {
        // Invalid credentials
        LoginClientDTO loginRequest = new LoginClientDTO("johndoe@example.com", "wrongpassword");

        mockMvc.perform(post("/auth/login-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isNotFound()) // Expecting a 404 Not Found for invalid credentials
                .andExpect(jsonPath("$.status").value("Error"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    public void testLoginClient_ValidationFailure() throws Exception {
        // Create a LoginClientDTO with invalid email and short password
        LoginClientDTO loginRequest = new LoginClientDTO("not-an-email", "123");
        String jsonContent = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login-client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

    }
}
