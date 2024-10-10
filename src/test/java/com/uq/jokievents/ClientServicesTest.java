package com.uq.jokievents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.JwtService;
import com.uq.jokievents.utils.Generators;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ClientServicesTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ClientRepository clientRepository;
    @Autowired private JwtService jwtService;

    @BeforeEach
    public void setup() {
        // Activate the client that will be deleted (Active set to false)
        Client client = clientRepository.findById("66f3b71c95dcb9591580d078").orElseThrow(null);
        client.setActive(true);
        clientRepository.save(client);
    }

    @Test
    public void testUpdateClient_Success() throws Exception {
        // The valid clientId for the request
        String clientId = "6706a5101654657267419fef"; // This is in the dataset.js file.

        String randomFactor = Generators.generateRndVerificationCode();

        // Create the request body
        UpdateClientDTO updateAdminDTO = new UpdateClientDTO(
                "3141" + randomFactor,
                "greatEmail" + randomFactor + "@example.com",
                "new-name-n3on" + randomFactor,
                "Casa de " + randomFactor
        );

        // Simulate a valid JWT token
        UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
        String currentToken = jwtService.getClientToken(clientDetails);

        // Perform the PUT request using MockMvc
        mockMvc.perform(post("/api/clients/6706a5101654657267419fef/update")
                        .header("Authorization","Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateAdminDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
    }

    @Test
    public void testDeleteClient_Success() throws Exception {
        // The valid clientId for the request
        String clientId = "66f3b71c95dcb9591580d078"; // This is in the dataset.js file.

        // Simulate a valid JWT token
        UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
        String currentToken = jwtService.getClientToken(clientDetails);

        // Perform the PUT request using MockMvc
        mockMvc.perform(post("/api/clients/{clientId}/delete", clientId)
                        .header("Authorization","Bearer " + currentToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
    }

    @Test
    public void testGetAccountInformation_Success() throws Exception {
        // The valid clientId for the request
        String clientId = "6706a5101654657267419fef"; // This is in the dataset.js file.

        // Simulate a valid JWT token
        UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
        String currentToken = jwtService.getClientToken(clientDetails);

        // Perform the PUT request using MockMvc
        mockMvc.perform(get("/api/clients/get-client-account-info/{clientId}", clientId)
                        .header("Authorization","Bearer " + currentToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
    }

    @Test
    public void testOrderLocality_Success() throws Exception {
        // The valid clientId for the request
        String clientId = "6706a5101654657267419fef"; // This is in the dataset.js file.
        String eventId = "67072eac29bb6b12ddff0f9d";
        // Simulate a valid JWT token
        UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
        String currentToken = jwtService.getClientToken(clientDetails);

        LocalityOrderAsClientDTO dto = new LocalityOrderAsClientDTO(
            eventId, "General Admission", 500, 10
        );

        // Perform the PUT request using MockMvc
        mockMvc.perform(post("/api/clients/order-locality/{clientId}", clientId)
                        .header("Authorization","Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
    }

    @Test
    public void testCancelLocalityOrder_Success() throws Exception {
        // The valid clientId for the request
        String clientId = "6706a5101654657267419fef"; // This is in the dataset.js file.
        String eventId = "67072eac29bb6b12ddff0f9d";

        // Simulate a valid JWT token
        UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
        String currentToken = jwtService.getClientToken(clientDetails);

        LocalityOrderAsClientDTO dto = new LocalityOrderAsClientDTO(
                eventId, "General Admission", 250, 5
        );

        // Perform the PUT request using MockMvc
        mockMvc.perform(post("/api/clients/cancel-locality-order/{clientId}", clientId)
                        .header("Authorization","Bearer " + currentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
    }

    @Test
    public void testLoadShoppingCart_Success() throws Exception {
        // The valid clientId for the request
        String clientId = "6706a5101654657267419fef"; // This is in the dataset.js file.

        // Simulate a valid JWT token
        UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
        String currentToken = jwtService.getClientToken(clientDetails);

        // Perform the PUT request using MockMvc
        mockMvc.perform(get("/api/clients/load-shopping-cart/{clientId}", clientId)
                        .header("Authorization","Bearer " + currentToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
    }
}


