package com.uq.jokievents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uq.jokievents.dtos.UpdateAdminDTO;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.service.implementation.JwtServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

    @Test
    public void testUpdateAdmin_Success() throws Exception {
        // The valid adminId for the request
        String adminId = "66f3aeb160c236c93c22b808";

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));    }
}
