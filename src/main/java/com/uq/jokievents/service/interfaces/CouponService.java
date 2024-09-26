package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.Coupon;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

public interface CouponService {
    
    ResponseEntity<?> findAllCoupons();
    ResponseEntity<?> findCouponById(String id);
    ResponseEntity<?> getAllCouponsPaginated(int page, int size);
    Optional<Coupon> findCouponByName(String name);
    Optional<Coupon> findCouponInstanceById(String couponId);
    Coupon saveCoupon(Coupon coupon);
    void deleteCouponById(String couponId);
    void deleteAllCoupons();
}
