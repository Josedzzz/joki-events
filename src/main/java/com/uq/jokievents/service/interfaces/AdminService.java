package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.model.Event;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

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
    ResponseEntity<?> getAllEventsPaginated(int page, int size);
    ResponseEntity<?> updateEvent(String id, HandleEventDTO dto);
    ResponseEntity<?> deleteEvent(String id);
    ResponseEntity<?> deleteAllEvents(); // Weird use case, but, everything is covered bbyboi!
    ResponseEntity<?> getAllCouponsPaginated(int page, int size);
}


