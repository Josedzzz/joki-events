package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.repository.CouponRepository;
import com.uq.jokievents.service.interfaces.CouponService;
import com.uq.jokievents.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;

    // TODO DEPRECATED
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

    @Override
    public ResponseEntity<?> getAllCouponsPaginated(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Coupon> eventPage = couponRepository.findAll(pageable);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", eventPage.getContent());
            responseData.put("totalPages", eventPage.getTotalPages());
            responseData.put("totalElements", eventPage.getTotalElements());
            responseData.put("currentPage", eventPage.getNumber());

            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Success", "Coupons retrieved successfully", responseData);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to retrieve coupons", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
