package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.dtos.RegisterClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;

import javax.validation.Valid;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    @Autowired
    private final ClientRepository clientRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationService verificationService;
    @Autowired
    private Utils utils;


    @Override
    public ResponseEntity<?> findAllClients() {
        try {
            List<Client> clients = clientRepository.findAll();
            ApiResponse<String> response = new ApiResponse<>("Success", "Clients found", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Clients not found", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> findClientById(String id) {
        try {
            Optional<Client> client = clientRepository.findById(id);
            if (client.isPresent()) {
                ApiResponse<Client> response = new ApiResponse<>("Success", "Client found", client.get());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Client not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<Client> response = new ApiResponse<>("Error", "Failed client request", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates a client from a dto.
     * @param id String
     * @param dto UpdateClientDTO
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<?> updateClient(String id, @Valid @RequestBody UpdateClientDTO dto) {
        System.out.println("Reached update client method");
        try {
            // Extracting the logged-in client from the security context
            Client loggedInClient = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String loggedInClientId = loggedInClient.getId(); // Get the client ID from the Client object
            System.out.println("Logged client id: " + loggedInClientId);

            // Check if the logged-in client is the same as the one being updated
            if (!loggedInClientId.equals(id) || !SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("CLIENT"))) {
                ApiResponse<String> response = new ApiResponse<>("Error", "You are not authorized to update this client", null);
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            Optional<Client> existingClient = clientRepository.findById(id);
            if (existingClient.isPresent()) {
                Client client = existingClient.get();

                // Verifications for Client update
                if (!client.getIdCard().equals(dto.idCard()) && utils.existsByIdCard(dto.idCard())) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "The identification card is in use", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                if (!client.getEmail().equals(dto.email()) && utils.existsEmailClient(dto.email())) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "The email is in use", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

                // Update client details
                client.setIdCard(dto.idCard());
                client.setPhoneNumber(dto.phone());
                client.setEmail(dto.email());
                client.setName(dto.name());
                client.setDirection(dto.direction());
                Client updatedClient = clientRepository.save(client);

                ApiResponse<Client> response = new ApiResponse<>("Success", "Client updated successfully", updatedClient);
                return new ResponseEntity<>(updatedClient, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Client not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to update client", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<?> deleteClient(String id) {
        try {
            Optional<Client> existingClient = clientRepository.findById(id);
            if (existingClient.isPresent()) {
                clientRepository.deleteById(id);
                ApiResponse<String> response = new ApiResponse<>("Success", "Client extermination complete", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Client not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete client", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> verifyCode(String clientId, @Valid VerifyClientDTO dto) {
        String verificationCode = dto.verificationCode();
        boolean verified = verificationService.verifyCode(clientId, verificationCode);
        if (verified) {
            Optional<Client> client = clientRepository.findById(clientId);
            if(client.isPresent()){
                Client unverifiedClient = client.get();
                unverifiedClient.setActive(true);
                clientRepository.save(unverifiedClient);
            }
            ApiResponse<String> response = new ApiResponse<>("Success", "Client verification done", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            ApiResponse<String> response = new ApiResponse<>("Error", "Invalid code or time expired", null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> existsByEmail(String email) {
        try {
            boolean exists = clientRepository.existsByEmail(email);
            if (exists) {
                ApiResponse<String> response = new ApiResponse<>("Error", "The email is in use", null);
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Success", "The email is available", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to check existence of email", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> existsByIdCard(String idCard) {
        try {
            boolean exists = clientRepository.existsByIdCard(idCard);
            if (exists) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Identification card is already in use", null);
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Success", "Identification card is available", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to check identification card", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
