package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.*;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    
    ResponseEntity<?> updateAdmin(String id, UpdateAdminDTO dto);
    ResponseEntity<?> deleteAdminById(String id);
    ResponseEntity<?> loginAdmin(AuthAdminDTO dto); // Fuck them dtos
    ResponseEntity<?> sendRecoverPasswordCode(String email);
    ResponseEntity<?> recoverPassword(RecoverPassAdminDTO dto);
    ResponseEntity<?> createCoupon(CreateCouponDTO dto);
    ResponseEntity<?> updateCoupon(String id, UpdateCouponDTO dto);
}
