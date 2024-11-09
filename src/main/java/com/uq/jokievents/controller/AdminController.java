package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.exceptions.AccountException;
import com.uq.jokievents.exceptions.AuthorizationException;
import com.uq.jokievents.exceptions.LogicException;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.service.interfaces.AdminService;

import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Validated
public class AdminController {

    // TODO 0auth login with Google Account
    // TODO Refresh Tokens
    // TODO Admin actions logger, can be one of the two additional functionalities
    private final AdminService adminService;

    @GetMapping("/get-all-admins")
    public ResponseEntity<ApiTokenResponse<?>> getAllAdmins() {
        try {
            ApiTokenResponse<Map<String, Object>> response = adminService.getAllAdmins();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", "Could not retrieve admins", null, null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{adminId}/update")
    public ResponseEntity<ApiTokenResponse<?>> updateAdmin(@PathVariable String adminId, @Valid @RequestBody UpdateAdminDTO dto) {
        try {
            ApiTokenResponse<Object> response = adminService.updateAdmin(adminId, dto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{adminId}/get-admin-account-info")
    public ResponseEntity<ApiResponse<?>> getAccountInformation(
            @PathVariable String adminId)       {
        try {
            ApiResponse<UpdateAdminDTO> response = adminService.getAccountInformation(adminId);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to retrieve admin info", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{adminId}/delete")
    public ResponseEntity<ApiResponse<?>> deleteAdminById(@PathVariable String adminId) {
        try {
            ApiResponse<String> response = adminService.deleteAdminAccount(adminId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create-coupon")
    public ResponseEntity<ApiResponse<?>> createCoupon(@RequestBody @Valid CreateCouponDTO dto) {
        try {
            ApiResponse<Coupon> response = adminService.createCoupon(dto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (AuthorizationException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to create coupon", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{couponId}/update-coupon")
    public ResponseEntity<ApiResponse<?>> updateCoupon(
            @PathVariable String couponId,
            @RequestBody @Valid UpdateCouponDTO dto) {
        try {
            ApiResponse<Coupon> response = adminService.updateCoupon(couponId, dto);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to update coupon", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-paginated-coupons")
    public ResponseEntity<ApiResponse<?>> getAllCouponsPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            ApiResponse<Map<String, Object>> response = adminService.getAllCouponsPaginated(page, size);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to retrieve coupons", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{couponId}/delete-coupon")
    public ResponseEntity<ApiResponse<String>> deleteCoupon(@PathVariable String couponId) {
        try {
            ApiResponse<String> response = adminService.deleteCoupon(couponId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to delete coupon", e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/delete-all-coupons")
    public ResponseEntity<ApiResponse<String>> deleteAllCoupons() {
        try {
            ApiResponse<String> response = adminService.deleteAllCoupons();
            return ResponseEntity.ok(response);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to delete all coupons", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/create-event")
    public ResponseEntity<ApiResponse<?>> addEvent(@RequestBody  @Valid HandleEventDTO dto) {
        try {
            ApiResponse<Event> response = adminService.addEvent(dto);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to create event", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-paginated-events")
    public ResponseEntity<ApiResponse<?>> getAllEventsPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size ) {
        try {
            ApiResponse<Map<String, Object>> response = adminService.getAllEventsPaginated(page, size);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to retrieve events", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{eventId}/update-event")
    public ResponseEntity<ApiResponse<?>> updateEvent(
            @PathVariable String eventId,
            @Valid @RequestBody HandleEventDTO dto) {
        try {
            ApiResponse<Event> response = adminService.updateEvent(eventId, dto);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to update event", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{eventId}/delete-event")
    public ResponseEntity<ApiResponse<String>> deleteEventById(@Valid @PathVariable String eventId) {
        try {
            ApiResponse<String> response = adminService.deleteEvent(eventId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (LogicException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    // POST until further notice.
    @PostMapping("/delete-all-events")
    public ResponseEntity<ApiResponse<String>> deleteAllEvents() {
        try {
            ApiResponse<String> response = adminService.deleteAllEvents();
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (LogicException e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to delete all events", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/event-report")
    public ResponseEntity<?> getEventsReport(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return null;
    }
}