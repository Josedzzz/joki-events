package com.uq.jokievents.service;

import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<Coupon> findAll() {
        return couponRepository.findAll();
    }

    /**
     * Gets a coupon by its unique id from the db
     *
     * @param id unique identifier of the coupon
     * @return an Optional containing the coupon if found, empty Optional if not
     */
    public Optional<Coupon> findById(String id) {
        return couponRepository.findById(id);
    }

    /**
     * Saves a new coupon or updates an existing in the db
     *
     * @param coupon the coupon object to be saved or updated
     * @return the saved or updated coupon object
     */
    public Coupon save(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    /**
     * Deletes a coupon from the db using its id
     *
     * @param id the unique identifier of the coupon to be deleted
     */
    public void deleteById(String id) {
        couponRepository.deleteById(id);
    }

}
