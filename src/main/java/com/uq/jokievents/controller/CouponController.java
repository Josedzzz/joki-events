package com.uq.jokievents.controller;

import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * Gets a list of all coupons
     *
     * @return a ResponseEntity containing the list of coupon objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        List<Coupon> coupons = couponService.findAll();
        return new ResponseEntity<>(coupons, HttpStatus.OK);
    }

    /**
     * Gets a coupon by its id
     *
     * @param id the unique identifier of the coupon
     * @return a ResponseEntity containing the coupon object and HTTP status of ok if found. otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Coupon> getCouponById(@PathVariable String id) {
        Optional<Coupon> coupon = couponService.findById(id);
        return coupon.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new Coupon
     *
     * @param coupon the coupon object to be created
     * @return a ResponseEntity containing
     */
    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody Coupon coupon) {
        Coupon newCoupon = couponService.save(coupon);
        return new ResponseEntity<>(newCoupon, HttpStatus.CREATED);
    }

    /**
     * Updates an existing coupon
     *
     * @param id identifier of the coupon to be updated
     * @param coupon the coupon object containing the updated data
     * @return a ResponseEntity containing the updated coupon object and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable String id, @RequestBody Coupon coupon) {
        Optional<Coupon> couponOptional = couponService.findById(id);
        if (couponOptional.isPresent()) {
            coupon.setId(id);
            Coupon newCoupon = couponService.save(coupon);
            return new ResponseEntity<>(newCoupon, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes a coupon by its id
     *
     * @param id the identifier of the coupon to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Coupon> deleteCoupon(@PathVariable String id) {
        couponService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
