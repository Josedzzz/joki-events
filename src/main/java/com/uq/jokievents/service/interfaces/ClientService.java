package com.uq.jokievents.service.interfaces;

import org.springframework.http.ResponseEntity;
import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.dtos.RegisterClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;


public interface ClientService {

    // Mucho "?"
    ResponseEntity<?> findAllClients();
    ResponseEntity<?> findClientById(String clientId);
    ResponseEntity<?> updateClient(String id, UpdateClientDTO client); // Shall I add the ID to the dto class?
    ResponseEntity<?> deleteClient(String id);
    ResponseEntity<?> loginClient(LoginClientDTO dto);
    ResponseEntity<?> registerNewClient(RegisterClientDTO dto);
    ResponseEntity<?> verifyCode(String clientId, VerifyClientDTO dto); // Shall I add the ID to the dto class? x2!
    ResponseEntity<?> existsByEmail(String email);
    ResponseEntity<?> existsByIdCard(String idCard);
}
