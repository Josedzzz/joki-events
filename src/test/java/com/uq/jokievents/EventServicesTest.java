//package com.uq.jokievents;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.uq.jokievents.dtos.SearchEventDTO;
//import com.uq.jokievents.model.enums.EventType;
//import com.uq.jokievents.repository.ClientRepository;
//import com.uq.jokievents.service.interfaces.JwtService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//public class EventServicesTest {
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private ObjectMapper objectMapper;
//    @Autowired private ClientRepository clientRepository;
//    @Autowired private JwtService jwtService;
//
//    @Test
//    public void testSearchEvents_Success() throws  Exception {
//
//        // Random client that will search for an event he would like to attend.
//        String clientId = "6706a5101654657267419fef"; // This is in the dataset.js file.
//
//        // Simulate a valid JWT token
//        UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
//        String currentToken = jwtService.getClientToken(clientDetails);
//
//        // Every value can be null, and it would bring all the events
//        // Can only give an EventType, and it would search for that event type in the database
//        // This search is very, very flexible.
//        SearchEventDTO searchEventDTO = new SearchEventDTO(
//                "Test",
//                "New York",
//                null,
//                null,
//                EventType.CONCERT
//        );
//        // Perform the PUT request using MockMvc
//        mockMvc.perform(post("/api/events/search-event")
//                        .header("Authorization","Bearer " + currentToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(searchEventDTO)))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("Success"));
//    }
//
//}
