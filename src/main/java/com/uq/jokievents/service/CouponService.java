package com.uq.jokievents.service;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;

    /**
     * Get a lis of all coupons from the db
     *
     * @return a list of all coupons objects in the db
     */
    public ResponseEntity<?> findAll() {
        try {
            List<Coupon> coupon = couponRepository.findAll();
            return new ResponseEntity<>(coupon, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed coupons request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a coupon by its unique id from the db
     *
     * @param id unique identifier of the coupon
     * @return an Optional containing the coupon if found, empty Optional if not
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<Coupon> coupon = couponRepository.findById(id);
            if (coupon.isPresent()) {
                return new ResponseEntity<>(coupon.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Coupons not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed coupons request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new coupon
     *
     * @param coupon the coupon object to be created
     * @return a ResponseEntity containing the created coupon object and an HTTP status
     */
    public ResponseEntity<?> create(Coupon coupon) {
        try {
            Coupon createdCoupon= couponRepository.save(coupon);
            return new ResponseEntity<>(createdCoupon, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create coupon", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing coupon by id
     *
     * @param id the identifierof the coupon to be update
     * @param coupon the updated coupon object
     * @return a ResponseEntity containing the updated client coupon and an HTTP status
     */
    public ResponseEntity<?> update(String id, Coupon coupon) {
        try {
            Optional<Coupon> existingCoupon = couponRepository.findById(id);
            if (existingCoupon.isPresent()) {
                coupon.setId(id);
                Coupon updatedCoupon = couponRepository.save(coupon);
                return new ResponseEntity<>(updatedCoupon, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Coupon not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update coupon", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a coupon from the db using its id
     *
     * @param id the unique identifier of the coupon to be deleted
     */
    public ResponseEntity<?> deleteById(String id) {
        try {
            Optional<Coupon> existingCoupon = couponRepository.findById(id);
            if (existingCoupon.isPresent()) {
                couponRepository.deleteById(id);
                return new ResponseEntity<>("Coupon deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Coupon not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to coupon client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
