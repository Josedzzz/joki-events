package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.records.RegisterClientDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public interface ClientService {

    ResponseEntity<?> findAllClients();
    ResponseEntity<?> findClientById(String id);
    ResponseEntity<?> updateClient(String id, Client client);
    ResponseEntity<?> deleteClient(String id);
    ResponseEntity<?> findClientByEmailAndPassword(String email, String password);
    ResponseEntity<Map<String, String>> registerNewClient(RegisterClientDTO dto);
    ResponseEntity<?> verifyCode(@RequestParam String clientId, @RequestParam String verificationCode);
    ResponseEntity<?> existsByEmail(String email);
    ResponseEntity<?> existsByIdCard(String idCard);
}
