package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.service.interfaces.AdminService;

import com.uq.jokievents.service.interfaces.AuthenticationService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    // TODO Authentication with Firebase.
    // TODO 0auth
    // TODO Refresh Tokens
    // TODO Admin actions logger, can be one of the two additional functionalities
    private final AdminService adminService;
    private final AuthenticationService authenticationService;
    /**
     * Example JSON:
     * {
     *  "username": "XD",
     *  "email": "mail@mail.com"
     * }
     * @param adminId String
     * @param dto UpdateAdminDTO
     * @return ResponseEntity
     */
    @PostMapping("/{adminId}/update/")
    public ResponseEntity<?> updateAdmin(@PathVariable String adminId, @Valid @RequestBody UpdateAdminDTO dto) {
        return adminService.updateAdmin(adminId, dto);
    }

    @DeleteMapping("/{adminId}/delete")
    public ResponseEntity<?> deleteAdminById(@PathVariable String adminId) {
        return adminService.deleteAdminAccount(adminId);
    }

    @PostMapping("/send-recover-code")
    public ResponseEntity<?> sendRecoverCode(@RequestParam String email) {
        return adminService.sendRecoverPasswordCode(email);
    }

    /**
     * Example JSON:
     * {
     *  "email": "mail@mail.com",
     *  "verificationCode": "123456",
     *  "newPassword": "new-non-encrypted-password"
     * }
     * @param dto RecoverPassAdminDTO
     * @return ResponseEntity
     */
    @PostMapping("/recover-password/")
    public ResponseEntity<?> recoverPassword(@Valid @RequestBody RecoverPassAdminDTO dto) {
        return adminService.recoverPassword(dto);
    }

    /**
     * Example input JSON:
     * {
     *     "name": "Summer Sale",
     *     "discount": 15.0,
     *     "expirationDate": "2024-12-31T23:59:59",
     *     "minPurchaseAmount": 100.0
     * }
     * Example output JSON:
     * {
     *     "status": "Success",
     *     "message": "Created coupon done",
     *     "data": {
     *         "id": "66e116c1f6751275233b24ff",
     *         "name": "Summer Sale",
     *         "discountPercent": 15.0,
     *         "expirationDate": "2024-12-31T23:59:59",
     *         "minPurchaseQuantity": 100.0,
     *         "used": false
     *     }
     * }
     * Either this or error messages and data would be always empty.
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
     *     "discount": 20.0,
     *     "expirationDate": "2024-12-31T23:59:59",
     *     "minPurchaseAmount": 110.0
     * }
     * Output JSON:
     * {
     *     "status": "Success",
     *     "message": "Coupon updated",
     *     "data": {
     *         "id": "66e116c1f6751275233b24ff",
     *         "name": "Summer Sale",
     *         "discountPercent": 20.0,
     *         "expirationDate": "2024-12-31T23:59:59",
     *         "minPurchaseAmount": 110.0,
     *         "used": false
     *     }
     * }
     * @param dto UpdateCouponDTO
     * @return ResponseEntity
     */
    @PostMapping("/update-coupon/{couponId}")
    public ResponseEntity<?> updateCoupon( @PathVariable String couponId, @Valid @RequestBody UpdateCouponDTO dto) {
        return  adminService.updateCoupon(couponId, dto);
    }

    // TODO Ask Jose if the path (delete-coupon) is necessary for this method to be used. Logic is tickling. Same question for deleteAllCoupons() below.
    @DeleteMapping("/delete-coupon/{couponId}")
    public ResponseEntity<?> deleteCouponById( @PathVariable String couponId) {
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

    @PostMapping("/update-event/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable String id, @Valid @RequestBody HandleEventDTO dto) {
        return adminService.updateEvent(id, dto);
    }

    @DeleteMapping("/delete-event/{id}")
    public ResponseEntity<?> deleteEventById(@Valid @PathVariable String id) {
        return adminService.deleteEvent(id);
    }

    @DeleteMapping("/delete-all-events")
    public ResponseEntity<?> deleteAllEvents(){
        return adminService.deleteAllEvents();
    }
}
