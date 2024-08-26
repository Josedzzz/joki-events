package com.uq.jokievents.service;

import java.time.LocalDateTime;
import java.util.*;

import com.uq.jokievents.records.RegisterClientDTO;
import com.uq.jokievents.utils.ClientMapper;
import com.uq.jokievents.utils.EmailService;
import com.uq.jokievents.utils.VerificationService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.ClientRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository; // Interacts with MongoDB
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerificationService verificationService;

    /**
     * Get a list of all clients from the db
     *
     * @return a ResponseEntity containing a list of Client objects and an HTTP status of ok
     */
    public ResponseEntity<?> findAll() {
        try {
            List<Client> clients = clientRepository.findAll();
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed clients request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a client by id
     *
     * @param id the identifier of the client object
     * @return a ResponseEntity containing the client and an HTTP status
     */
    public ResponseEntity<?> findById(String id) {
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
     * Create a new client
     *
     * @param client the client object to be created
     * @return a ResponseEntity containing the created client object and an HTTP status
     */
    public ResponseEntity<?> create(Client client) {
        try {
            Client createdClient = clientRepository.save(client);
            return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifier of the client to be updated
     * @param client the updated client object
     * @return a ResponseEntity containing the updated client object and an HTTP status
     */
    public ResponseEntity<?> update(String id, Client client) {
        try {
            Optional<Client> existingClient = clientRepository.findById(id);
            if (existingClient.isPresent()) {
                client.setId(id);
                Client updatedClient = clientRepository.save(client);
                return new ResponseEntity<>(updatedClient, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a client by its id
     *
     * @param id the identifier of the client to delete
     * @return a ResponseEntity with an HTTP status
     */
    public ResponseEntity<?> delete(String id) {
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

    /**
     * Find a client by email and password
     *
     * @param email of the client
     * @param password of the client
     * @return a ResponseEntity containing a JSON with the client's id if found, otherwise a JSON with an error message
     */
    public ResponseEntity<?> findByEmailAndPassword(String email, String password) {
        try {
            Optional<Client> client = clientRepository.findByEmailAndPassword(email, password);
            if (client.isPresent()) {
                //Verificates if the Client is active for login
                if(!client.get().isActive()){
                    Map<String, String> response = new HashMap<>();
                    response.put("id", client.get().getId());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
                else{
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("message", "The client isn´t active");
                    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
                }
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid email or password");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to find client");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Registers a client from its dto instance.
     * Is it necessary to have a dto parameter? Couldn't it be the Client class itself?
     * @return a client
     */
    public Client registerNewClient(RegisterClientDTO dto) {
        // Mapping the DTO as an entity (Client)
        Client client = ClientMapper.INSTANCE.ClientRegisterDTOtoClient(dto);

        // Generating a verification code and establishing an expiration date
        String verificationCode = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);

        // Manually assigning all the other attributes.
        client.setVerificationCode(verificationCode);
        client.setVerificationCodeExpiration(expiration);

        client.setIdCoupons(new ArrayList<ObjectId>());
        client.setIdShoppingCart(new ObjectId());
        client.setActive(true);

        // Sending the email to the client!
        emailService.sendVerificationMail(client.getEmail(), verificationCode);

        // Save the Client to the database.
        client = clientRepository.save(client);
        // Return the saved Client
        return client;
    }

    /**
     * Method to answer a http request to verify if a code is valid or not.
     * @param clientId client id which is validating its account
     * @param verificationCode verification code of the client
     * @return boolean saying if verified or not
     */
    @PostMapping("/verify")
    public ResponseEntity<String> verifyCode(@RequestParam String clientId, @RequestParam String verificationCode) {
        boolean verified = verificationService.verifyCode(clientId, verificationCode);

        if (verified) {
            return new ResponseEntity<>("Client verified", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid code or time expired", HttpStatus.BAD_REQUEST);
        }
    }
}
