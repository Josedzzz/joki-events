package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.repository.CouponRepository;
import com.uq.jokievents.service.interfaces.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    @Autowired
    private final CouponRepository couponRepository;

    @Override
    public ResponseEntity<?> findAllCoupons() {
        try {
            List<Coupon> coupons = couponRepository.findAll();
            return new ResponseEntity<>(coupons, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed coupons request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> findCouponById(String id) {
        try {
            Optional<Coupon> coupon = couponRepository.findById(id);
            if (coupon.isPresent()) {
                return new ResponseEntity<>(coupon.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Coupon not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed coupon request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
