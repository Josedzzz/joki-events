package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.service.interfaces.CouponService;
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.JwtService;
import com.uq.jokievents.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

import javax.validation.Valid;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final VerificationService verificationService;
    private final Utils utils;
    private final JwtService jwtService;
    private final EventService eventService;
    private final CouponService couponService;

    /**
     * Updates a client from a dto.
     * @param clientId String
     * @param dto UpdateClientDTO
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<?> updateClient(String clientId, @RequestBody UpdateClientDTO dto) {

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            Optional<Client> existingClient = clientRepository.findById(clientId);
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

                // Actualizo el token también
                Client updatedClient = clientRepository.save(client);
                UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
                String newToken = jwtService.getAdminToken(clientDetails);

                ApiTokenResponse<Object> response = new ApiTokenResponse<>("Success","Admin update done", updatedClient, newToken);
                return new ResponseEntity<>(response, HttpStatus.OK);
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
    public ResponseEntity<?> deleteAccount(String clientId) {

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            Optional<Client> existingClient = clientRepository.findById(clientId);
            if (existingClient.isPresent()) {
                clientRepository.deleteById(clientId);
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

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if (verificationResponse != null) {
            return verificationResponse;
        }

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

    @Override
    public ResponseEntity<?> getAllEventsPaginated(int page, int size) {
        return eventService.getAllEventsPaginated(page, size);
    }
}
