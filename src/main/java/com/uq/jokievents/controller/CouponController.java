package com.uq.jokievents.controller;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.service.interfaces.CouponService;
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
    public ResponseEntity<?> findAllCoupons() {
        return couponService.findAllCoupons();
    }

    /**
     * Gets a coupon by its id
     *
     * @param id the unique identifier of the coupon
     * @return a ResponseEntity containing the coupon object and HTTP status of ok if found. otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findCouponById(@PathVariable String id) {
        return couponService.findCouponById(id);
    }


}
