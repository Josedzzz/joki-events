package com.uq.jokievents.service.interfaces;

import org.springframework.http.ResponseEntity;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;


public interface ClientService {
    ResponseEntity<?> updateClient(String clientId, UpdateClientDTO dto);
    ResponseEntity<?> deleteAccount(String id);
    ResponseEntity<?> verifyCode(String clientId, VerifyClientDTO dto);
    ResponseEntity<?> existsByEmail(String email);
    ResponseEntity<?> existsByIdCard(String idCard);
}
