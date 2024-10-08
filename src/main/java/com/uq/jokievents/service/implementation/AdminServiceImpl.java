package com.uq.jokievents.service.implementation;

import javax.validation.Valid;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.model.Locality;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.repository.LocalityRepository;
import com.uq.jokievents.service.interfaces.*;
import com.uq.jokievents.utils.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.repository.AdminRepository;
import com.uq.jokievents.repository.CouponRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final AdminRepository adminRepository;
    private final EmailService emailService;
    private final EventService eventService;
    private final PasswordEncoder passwordEncoder;
    private final CouponService couponService;
    private final JwtService jwtService;

    @Override
    public ResponseEntity<?> updateAdmin(String adminId, @RequestBody UpdateAdminDTO dto) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithId(adminId);
        if (verificationResponse != null) {
            return verificationResponse;
        }
        Admin admin = new Admin(); // je réserve la mémoire
        try {
            // This optional will always bring the Admin object, as the admin is the one updating itself.
            Optional<Admin> existingAdmin = adminRepository.findById(adminId);
            if (existingAdmin.isPresent()) {
                admin = existingAdmin.get(); // This conditional will never fail, just protocol
            }
            admin.setUsername(dto.username());
            admin.setEmail(dto.email());

            Admin updatedAdmin = adminRepository.save(admin);
            // Generar nuevo token con los datos actualizados
            UserDetails adminDetails = adminRepository.findById(adminId).orElse(null);
            String newToken = jwtService.getAdminToken(adminDetails);
            // Devolver la respuesta con Admin en 'data' y el token en 'token'
            ApiTokenResponse<Object> response = new ApiTokenResponse<>("Success","Admin update done", updatedAdmin, newToken);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to update admin", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> deleteAdminAccount(String adminId) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithId(adminId);
        if (verificationResponse != null) {
            return verificationResponse;
        }
        Admin admin = new Admin(); // je réserve la mémoire
        try {
            Optional<Admin> existingAdmin = adminRepository.findById(adminId);
            if (existingAdmin.isPresent()) admin = existingAdmin.get();
            admin.setActive(false);
            adminRepository.save(admin);
            ApiResponse<String> response = new ApiResponse<>("Success", "Admin account deactivated", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            // Would only output if the database fails
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete admin", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> sendRecoverPasswordCode(String email) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            // Validate if the admin exists by email
            Optional<Admin> adminOptional = adminRepository.findByEmail(email);
            if (adminOptional.isEmpty()) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Admin not found.", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Admin admin = adminOptional.get();

            // Generate a new verification code
            String verificationCode = Generators.generateRndVerificationCode();

            // Set the expiration time to 20 minutes from now
            admin.setVerificationCode(verificationCode);
            admin.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(20));

            // Save the updated admin with the verification code and expiration time
            adminRepository.save(admin);

            // Send the recovery email
            emailService.sendRecuperationEmail(admin.getEmail(), verificationCode);

            ApiResponse<String> response = new ApiResponse<>("Success", "Recovery code sent", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to send recovery code", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> recoverPassword(@Valid RecoverPassAdminDTO dto) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            // Validate if the user exists
            String email = dto.email();
            String verificationCode =  dto.verificationCode();
            String newPassword = passwordEncoder.encode(dto.newPassword());
            Optional<Admin> adminOptional = adminRepository.findByEmail(email);

            System.out.println("404 ALLEGATION" + adminOptional.get().toString());

            if (adminOptional.isEmpty()) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Admin not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Admin admin = adminOptional.get();
            // Check if the verification code is expired
            if (admin.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Verification code has expired", null);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
            // Verify if the code matches (assuming the admin entity has a verification code field) (Jose will make sure of it)
            if (!admin.getVerificationCode().equals(verificationCode)) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Invalid verification code", null);
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            // Update the password
            admin.setPassword(newPassword);
            admin.setVerificationCode("");
            adminRepository.save(admin);

            ApiResponse<String> response = new ApiResponse<>("Success", "Password recovery done", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Password recovery failed", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> createCoupon(CreateCouponDTO dto) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        // Check if a coupon with the same localityName already exists
        Optional<Coupon> existingCoupon = couponService.findCouponByName(dto.name());

        if (existingCoupon.isPresent()) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Coupon with the same name already exists", null);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        Coupon coupon = new Coupon();
        coupon.setName(dto.name());
        coupon.setDiscountPercent(dto.discount());
        coupon.setExpirationDate(dto.expirationDate());
        coupon.setMinPurchaseAmount(dto.minPurchaseAmount());

        Coupon savedCoupon = couponService.saveCoupon(coupon);
        ApiResponse<Coupon> response = new ApiResponse<>("Success", "Coupon creation done", savedCoupon);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<?> updateCoupon(String couponId, @Valid UpdateCouponDTO dto) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        Coupon coupon = new Coupon();

        try {
            Optional<Coupon> optionalCoupon = couponService.findCouponInstanceById(couponId);
            if (optionalCoupon.isPresent()) {
                coupon = optionalCoupon.get();
            }
            // Update the fields
            coupon.setDiscountPercent(dto.discount());
            coupon.setExpirationDate(dto.expirationDate());
            coupon.setMinPurchaseAmount(dto.minPurchaseAmount());

            // Save the updated coupon
            Coupon updatedCoupon = couponService.saveCoupon(coupon);
            ApiResponse<Coupon> response = new ApiResponse<>("Success", "Coupon updated", updatedCoupon);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to update coupon", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> deleteCoupon(String couponId){

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            Optional<Coupon> existingCoupon = couponService.findCouponInstanceById(couponId); // This is basically "couponRepository.findById(couponId);"
            if (existingCoupon.isPresent()) {
                couponService.deleteCouponById(couponId);
                ApiResponse<String> response = new ApiResponse<>("Success", "Coupon deleted", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                // Could this ever happen? No, don't mind checking this in a test future Daniel.
                ApiResponse<String> response = new ApiResponse<>("Error", "Coupon not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete coupon", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // TODO Tell Jose this exists, haven't told him yet
    public ResponseEntity<?> deleteAllCoupons() {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            couponService.deleteAllCoupons();
            ApiResponse<String> response = new ApiResponse<>("Success", "All coupons deleted", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete all coupons", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> addEvent(HandleEventDTO dto) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        // Delegating the  event creation to EventService as god intended or maybe not, makes sense for it to be here as it is a sole admin function. No client can create an event.
        return eventService.addEvent(dto);
    }

    @Override
    public ResponseEntity<?> getAllEventsPaginated(int page, int size) {
        return eventService.getAllEventsPaginated(page, size);
    }

    @Override
    public ResponseEntity<?> updateEvent(String eventId, @Valid HandleEventDTO dto) {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }
        return eventService.updateEvent(eventId, dto);
    }

    public ResponseEntity<?> deleteEvent(String eventId){

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            Optional<Event> existingEvent = eventService.getEventById(eventId); // Long life to SRP
            if (existingEvent.isPresent()) {
                eventService.deleteEventById(eventId);
                ApiResponse<String> response = new ApiResponse<>("Success", "Event deleted", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Event not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete Event", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> deleteAllEvents() {

        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            eventService.deleteAllEvents();
            ApiResponse<String> response = new ApiResponse<>("Success", "All events deleted", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete all events", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllCouponsPaginated(int page, int size) {
        return couponService.getAllCouponsPaginated(page, size);
    }

    @Override
    public ResponseEntity<?> getAccountInformation(String adminId) {
        ResponseEntity<?> verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }
        try {
            Optional<Admin> admin = adminRepository.findById(adminId);
            if (admin.isPresent()) {
                String username = admin.get().getUsername();
                String email = admin.get().getEmail();

                UpdateAdminDTO dto = new UpdateAdminDTO(username, email);
                ApiResponse<UpdateAdminDTO> response = new ApiResponse<>("Success", "Admin info returned", dto);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Admin info not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to retrieve admin info", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> generateEventsReport(String startDate, String endDate) {
        LocalDateTime ldtStartDate = LocalDateTime.parse(startDate);
        LocalDateTime ldtEndDate = LocalDateTime.parse(endDate);
        return eventService.generateEventsReport(ldtStartDate, ldtEndDate);
    }
}
