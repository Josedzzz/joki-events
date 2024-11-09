package com.uq.jokievents.repository;

import com.uq.jokievents.model.Coupon;

import java.util.List;
import java.util.Optional;

import com.uq.jokievents.model.enums.CouponType;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {
    Optional<Coupon> findByName(String name);
    List<Coupon> findByCouponType(CouponType couponType);
}
