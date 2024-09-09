package com.uq.jokievents.service.interfaces;

import org.springframework.http.ResponseEntity;

import com.uq.jokievents.dtos.AuthAdminDTO;
import com.uq.jokievents.dtos.RecoverPassAdminDTO;
import com.uq.jokievents.dtos.UpdateAdminDTO;

public interface AdminService {
    
    ResponseEntity<?> updateAdmin(String id, UpdateAdminDTO dto);
    ResponseEntity<?> deleteAdminById(String id);
    ResponseEntity<?> loginAdmin(AuthAdminDTO dto); // Fuck them dtos
    ResponseEntity<?> sendRecoverPasswordCode(String email);
    ResponseEntity<?> recoverPassword(RecoverPassAdminDTO dto);
}
