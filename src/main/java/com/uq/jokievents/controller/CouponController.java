package com.uq.jokievents.controller;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * Gets a list of all coupons
     *
     * @return a ResponseEntity containing the list of coupon objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<?> getAllCoupons() {
        return couponService.findAll();
    }

    /**
     * Gets a coupon by its id
     *
     * @param id the unique identifier of the coupon
     * @return a ResponseEntity containing the coupon object and HTTP status of ok if found. otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCouponById(@PathVariable String id) {
        return couponService.findById(id);
    }

    /**
     * Creates a new Coupon
     *
     * @param coupon the coupon object to be created
     * @return a ResponseEntity containing
     */
    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        return couponService.create(coupon);
    }

    /**
     * Update an existing coupon by id
     *
     * @param id the identifier of the client to update
     * @param coupon the updated coupon object
     * @return a ReponseEntity containing the update client
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable String id, @RequestBody Coupon coupon) {
        return couponService.update(id, coupon);
    }

    /**
     * Deletes a coupon by its id
     *
     * @param id the identifier of the coupon to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable String id) {
        return couponService.deleteById(id);
    }

}
