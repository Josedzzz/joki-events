package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.service.interfaces.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    // TODO 0auth login with Google Account
    // TODO Refresh Tokens
    // TODO Admin actions logger, can be one of the two additional functionalities
    private final AdminService adminService;

    /**
     * Example JSON:
     * {
     *  "username": "XD",
     *  "email": "mail@mail.com"
     * }
     *
     * @param adminId String
     * @param dto     UpdateAdminDTO
     * @return ResponseEntity
     */
    @PostMapping("/{adminId}/update")
    public ResponseEntity<?> updateAdmin(@PathVariable String adminId, @Valid @RequestBody UpdateAdminDTO dto) {
        return adminService.updateAdmin(adminId, dto);
    }

    @DeleteMapping("/{adminId}/delete")
    public ResponseEntity<?> deleteAdminById(@PathVariable String adminId) {
        return adminService.deleteAdminAccount(adminId);
    }

    /**
     * Example input JSON:
     * {
     * "localityName": "Summer Sale",
     * "discount": 15.0,
     * "expirationDate": "2024-12-31T23:59:59",
     * "minPurchaseAmount": 100.0
     * }
     * Example output JSON:
     * {
     * "status": "Success",
     * "message": "Created coupon done",
     * "data": {
     * "id": "66e116c1f6751275233b24ff",
     * "localityName": "Summer Sale",
     * "discountPercent": 15.0,
     * "expirationDate": "2024-12-31T23:59:59",
     * "minPurchaseQuantity": 100.0,
     * "used": false
     * }
     * }
     * Either this or error messages and data would be always empty.
     *
     * @param dto CreateCouponDTO
     * @return ResponseEntity
     */
    @PostMapping("/create-coupon")
    public ResponseEntity<?> createCoupon(@Valid @RequestBody CreateCouponDTO dto) {
        return adminService.createCoupon(dto);
    }

    /**
     * Input JSON:
     * {
     * "discount": 20.0,
     * "expirationDate": "2024-12-31T23:59:59",
     * "minPurchaseAmount": 110.0
     * }
     * Output JSON:
     * {
     * "status": "Success",
     * "message": "Coupon updated",
     * "data": {
     * "id": "66e116c1f6751275233b24ff",
     * "couponName": "Summer Sale",
     * "discountPercent": 20.0,
     * "expirationDate": "2024-12-31T23:59:59",
     * "minPurchaseAmount": 110.0,
     * "used": false
     * }
     * }
     *
     * @param dto UpdateCouponDTO
     * @return ResponseEntity
     */
    @PostMapping("/update-coupon/{couponId}")
    public ResponseEntity<?> updateCoupon(@PathVariable String couponId, @Valid @RequestBody UpdateCouponDTO dto) {
        return adminService.updateCoupon(couponId, dto);
    }

    @DeleteMapping("/delete-coupon/{couponId}")
    public ResponseEntity<?> deleteCouponById(@PathVariable String couponId) {
        return adminService.deleteCoupon(couponId);
    }

    @DeleteMapping("/delete-all-coupons")
    public ResponseEntity<?> deleteAllCoupons() {
        return adminService.deleteAllCoupons();
    }

    @PostMapping("/create-event")
    public ResponseEntity<?> createEvent(@Valid @RequestBody HandleEventDTO dto) {
        return adminService.addEvent(dto);
    }

    @GetMapping("/get-paginated-events")
    public ResponseEntity<?> getAllEventsPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        return adminService.getAllEventsPaginated(page, size);
    }

    @PostMapping("/update-event/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @Valid @RequestBody HandleEventDTO dto) {
        return adminService.updateEvent(id, dto);
    }

    // POST until further notice.
    @PostMapping("/delete-event/{id}")
    public ResponseEntity<?> deleteEventById(@Valid @PathVariable String id) {
        return adminService.deleteEvent(id);
    }

    // POST until further notice.
    @PostMapping("/delete-all-events")
    public ResponseEntity<?> deleteAllEvents() {
        return adminService.deleteAllEvents();
    }

    @GetMapping("/get-paginated-coupons")
    public ResponseEntity<?> getAllCouponsPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int size) {
        return adminService.getAllCouponsPaginated(page, size);
    }

    @GetMapping("/get-admin-account-info/{adminId}")
    public ResponseEntity<?> getLoginInformation(@PathVariable String adminId) {
        return adminService.getAccountInformation(adminId);
    }

    @GetMapping("/event-report")
    public ResponseEntity<?> getEventsReport(@RequestParam String startDate, @RequestParam String endDate) {
        return adminService.generateEventsReport(startDate, endDate);
    }
}