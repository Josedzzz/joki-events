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
    public ResponseEntity<?> updateClient(String id, @Valid @RequestBody UpdateClientDTO dto) {


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
                    return new ResponseEntity<>(client.get().getId(), HttpStatus.OK);
                }
                else{
                    return new ResponseEntity<>("The client isn't active", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>("Invalid email or password", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to find client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> registerNewClient(RegisterClientDTO dto) {

        // Mapping the DTO as an entity (Client)
        Client client = ClientMapper.INSTANCE.ClientRegisterDTOtoClient(dto);

        // Verifications for Client registration
        if(utils.existsByIdCard(client.getIdCard())){
            return new ResponseEntity<>("The identification card is in use", HttpStatus.BAD_REQUEST);
        }
        if(utils.existsEmailClient(client.getEmail())){
            return new ResponseEntity<>("The email is in use", HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity<>(client.getId(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> verifyCode(String clientId, VerifyClientDTO dto) {
        String verificationCode = dto.getVerificationCode();
        boolean verified = verificationService.verifyCode(clientId, verificationCode);
        if (verified) {
            return new ResponseEntity<>("Client verified", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Invalid code or time expired", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<?> existsByEmail(String email) {
        try {
            boolean exists = clientRepository.existsByEmail(email);
            if (exists) {
                return new ResponseEntity<>("Email is already in use", HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>("Email is available", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to check email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> existsByIdCard(String idCard) {
        try {
            boolean exists = clientRepository.existsByIdCard(idCard);
            if (exists) {
                return new ResponseEntity<>("Identification card is already in use", HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>("Identification card is available", HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to check identification card", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
