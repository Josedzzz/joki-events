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
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed client request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> findClientById(String id) {
        try {
            Optional<Client> client = clientRepository.findById(id);
            if (client.isPresent()) {
                return new ResponseEntity<>(client.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed client request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates a client from a dto.
     * @param id String
     * @param client UpdateClientDTO
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<?> updateClient(String id, UpdateClientDTO dto) {


        try {
            Optional<Client> existingClient = clientRepository.findById(id);
            if (existingClient.isPresent()) {

                Client client = existingClient.get();
                client.setIdCard(dto.getIdCard());
                client.setPhoneNumber(dto.getPhone());
                client.setEmail(dto.getEmail());
                client.setName(dto.getName());
                client.setDirection(dto.getDirection());
                Client updatedClient = clientRepository.save(client);   
                return new ResponseEntity<>(updatedClient, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update client", HttpStatus.INTERNAL_SERVER_ERROR);
        }   
    }

    @Override
    public ResponseEntity<?> deleteClient(String id) {
        try {
            Optional<Client> existingClient = clientRepository.findById(id);
            if (existingClient.isPresent()) {
                clientRepository.deleteById(id);
                return new ResponseEntity<>("Client deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> findClientByEmailAndPassword(LoginClientDTO dto) {
        try {
            String email = dto.getEmail();
            String password = dto.getPassword();
            Optional<Client> client = clientRepository.findByEmailAndPassword(email, password);
            if (client.isPresent()) {
                //Checks if the Client is active for login
                if(client.get().isActive()){
                    Map<String, String> response = new HashMap<>();
                    response.put("id", client.get().getId());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
                else{
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("Error", "The client isn't active");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("Error", "Invalid email or password");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("Error", "Failed to find client");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Map<String, String>> registerNewClient(RegisterClientDTO dto) {

        // Mapping the DTO as an entity (Client)
        Client client = ClientMapper.INSTANCE.ClientRegisterDTOtoClient(dto);

        // Verifications for Client registration
        if(utils.existsByIdCard(client.getIdCard())){
            Map<String, String> response = new HashMap<>();
            response.put("Error", "The identification card is in use");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if(utils.existsEmailClient(client.getEmail())){
            Map<String, String> response = new HashMap<>();
            response.put("Error", "The email is in use");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Generating a verification code and establishing an expiration date
        String verificationCode = Generators.generateRndVerificationCode();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);


        // Manually assigning all the other attributes.
        client.setVerificationCode(verificationCode);
        client.setVerificationCodeExpiration(expiration);

        client.setIdCoupons(new ArrayList<>());
        client.setIdShoppingCart(new ObjectId());
        client.setActive(false);

        // Sending the email to the client!
        emailService.sendVerificationMail(client.getEmail(), verificationCode);

        // Save the Client to the database.
        client = clientRepository.save(client);

        // Returns a response entity
        Map<String, String> response = new HashMap<>();
        response.put("id", client.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> verifyCode(String clientId, VerifyClientDTO dto) {
        String verificationCode = dto.getVerificationCode();
        boolean verified = verificationService.verifyCode(clientId, verificationCode);
        Map<String, String> response = new HashMap<>();
        if (verified) {
            response.put("Success", "Client verified");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("Error", "Invalid code or time expired");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> existsByEmail(String email) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean exists = clientRepository.existsByEmail(email);
            if (exists) {
                response.put("Error", "Email is already in use");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                response.put("Success", "Email is available");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            response.put("Error", "Failed to check email");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> existsByIdCard(String idCard) {
        Map<String, String> response = new HashMap<>();
        try {
            boolean exists = clientRepository.existsByIdCard(idCard);
            if (exists) {
                response.put("Error", "Identification card is already in use");
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                response.put("Success", "Identification card is available");
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            response.put("Error", "Failed to check idCard");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
