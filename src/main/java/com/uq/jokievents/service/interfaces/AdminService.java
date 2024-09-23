package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.*;
import org.springframework.http.ResponseEntity;

public interface AdminService {
    
    ResponseEntity<?> updateAdmin(String adminId, UpdateAdminDTO dto);
    ResponseEntity<?> deleteAdminAccount(String adminId);
    ResponseEntity<?> sendRecoverPasswordCode(String email);
    ResponseEntity<?> recoverPassword(RecoverPassAdminDTO dto);
    ResponseEntity<?> createCoupon(CreateCouponDTO dto);
    ResponseEntity<?> updateCoupon(String couponId, UpdateCouponDTO dto);
    ResponseEntity<?> deleteCoupon(String couponId);
    ResponseEntity<?> deleteAllCoupons();
    ResponseEntity<?> addEvent(HandleEventDTO dto);
    ResponseEntity<?> updateEvent(String id, HandleEventDTO dto);
    ResponseEntity<?> deleteEvent(String id);
    ResponseEntity<?> deleteAllEvents(); // Weird use case, but, everything is covered bbyboi!
}
