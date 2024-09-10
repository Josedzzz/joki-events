package com.uq.jokievents.service.interfaces;

import org.springframework.http.ResponseEntity;

public interface CouponService {
    
    ResponseEntity<?> findAllCoupons();
    ResponseEntity<?> findCouponById(String id);
}
