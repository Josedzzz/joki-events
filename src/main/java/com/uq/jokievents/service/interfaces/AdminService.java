package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.*;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    
    ResponseEntity<?> updateAdmin(String id, UpdateAdminDTO dto);
    ResponseEntity<?> deleteAdminAccount(String id);
    ResponseEntity<?> sendRecoverPasswordCode(String email);
    ResponseEntity<?> recoverPassword(RecoverPassAdminDTO dto);
    ResponseEntity<?> createCoupon(CreateCouponDTO dto);
    ResponseEntity<?> updateCoupon(String id, UpdateCouponDTO dto);
    ResponseEntity<?> deleteCoupon(String id);
    ResponseEntity<?> deleteAllCoupons();
    ResponseEntity<?> addEvent(HandleEventDTO dto);
    ResponseEntity<?> updateEvent(String id, HandleEventDTO dto);
    ResponseEntity<?> deleteEvent(String id);
    ResponseEntity<?> deleteAllEvents(); // Weird use case, but, everything is covered bbyboi!
}
