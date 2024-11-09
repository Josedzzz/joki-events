package com.uq.jokievents.service.implementation;

import javax.validation.Valid;

import com.uq.jokievents.dtos.*;
import com.uq.jokievents.exceptions.AccountException;
import com.uq.jokievents.exceptions.AuthorizationException;
import com.uq.jokievents.exceptions.LogicException;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.Locality;
import com.uq.jokievents.repository.CouponRepository;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.service.interfaces.*;
import com.uq.jokievents.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Coupon;
import com.uq.jokievents.repository.AdminRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final AdminRepository adminRepository;
    private final EventRepository eventRepository;
    private final CouponRepository couponRepository;
    private final ImageService imageService;
    private final JwtService jwtService;

    @Override
    public ApiTokenResponse<Object> updateAdmin(String adminId, UpdateAdminDTO dto) {
        // todo fix the logic
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to access this endpoint");
        }

        try {
            // Fetch the admin record
            Admin admin = adminRepository.findById(adminId).orElseThrow(() ->
                    new AccountException("Admin not found"));

            // Update fields
            admin.setUsername(dto.username());
            admin.setEmail(dto.email());

            // Save the updated admin record
            adminRepository.save(admin);

            // Generate new token with updated details
            UserDetails adminDetails = adminRepository.findById(adminId).orElseThrow();
            String newToken = jwtService.getAdminToken(adminDetails);

            return new ApiTokenResponse<Object>("Success", "Admin updated", admin, newToken);
        } catch (AccountException e) {
            throw new AccountException("Failed to update admin, serverside: " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<String> deleteAdminAccount(String adminId) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithId(adminId);
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to delete this account");
        }

        try {
            // Fetch the admin record
            Admin admin = adminRepository.findById(adminId).orElseThrow(() ->
                    new AccountException("Admin not found"));

            // Deactivate the account
            admin.setActive(false);
            adminRepository.save(admin);
            return new ApiResponse<>("Success", "Event deleted", null);
        } catch (AccountException e) {
            // Rethrow as a custom exception for the controller to handle
            throw new AccountException("Failed to deactivate admin account, serverside: " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<Coupon> createCoupon(CreateCouponDTO dto) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to create this coupon");
        }

        // Check if a coupon with the same name already exists
        Optional<Coupon> existingCoupon = couponRepository.findById(dto.name());
        if (existingCoupon.isPresent()) {
            throw new LogicException("Coupon with the same name already exists");
        }

        // Create and save the new coupon
        Coupon coupon = new Coupon();
        coupon.setName(dto.name());
        coupon.setDiscountPercent(dto.discount());
        coupon.setExpirationDate(dto.expirationDate());
        coupon.setMinPurchaseAmount(dto.minPurchaseAmount());
        Coupon savedCoupon = couponRepository.save(coupon);

        // Return success response
        return new ApiResponse<>("Success", "Coupon creation done", savedCoupon);
    }


    @Override
    public ApiResponse<Coupon> updateCoupon(String couponId, UpdateCouponDTO dto) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to update this coupon");
        }

        // Retrieve and update the coupon
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new LogicException("Coupon not found"));

        // Update the coupon fields
        coupon.setDiscountPercent(dto.discount());
        coupon.setExpirationDate(dto.expirationDate());
        coupon.setMinPurchaseAmount(dto.minPurchaseAmount());

        // Save and return updated coupon
        Coupon updatedCoupon = couponRepository.save(coupon);
        return new ApiResponse<>("Success", "Coupon updated", updatedCoupon);
    }


    @Override
    public ApiResponse<String> deleteCoupon(String couponId) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to delete this coupon");
        }

        // Check if the coupon exists
        couponRepository.findById(couponId)
                .orElseThrow(() -> new LogicException("Coupon not found"));

        // Delete the coupon
        couponRepository.deleteById(couponId);
        return new ApiResponse<>("Success", "Coupon deleted", null);
    }


    @Override
    public ApiResponse<String> deleteAllCoupons() {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to delete coupons");
        }

        try {
            couponRepository.deleteAll();
            return new ApiResponse<>("Success", "All coupons deleted", null);
        } catch (Exception e) {
            throw new LogicException("Failed to delete all coupons");
        }
    }


    @Override
    public ApiResponse<Event> addEvent(HandleEventDTO dto) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to create an event");
        }

        try {
            // Upload images and create event
            String imageUrl = imageService.uploadImage(dto.eventImageUrl());
            String localitiesUrl = imageService.uploadImage(dto.localitiesImageUrl());

            Event event = Event.builder()
                    .name(dto.name())
                    .address(dto.address())
                    .city(dto.city())
                    .eventDate(dto.date())
                    .availableForPurchase(true)  // Event available for purchase
                    .localities(dto.localities().stream().map(localityDTO ->
                            Locality.builder()
                                    .name(localityDTO.name())
                                    .price(localityDTO.price())
                                    .maxCapacity(localityDTO.maxCapacity())
                                    .build()
                    ).collect(Collectors.toList()))
                    .totalAvailablePlaces(dto.totalAvailablePlaces())
                    .eventImageUrl(imageUrl)
                    .localitiesImageUrl(localitiesUrl)
                    .eventType(dto.eventType())
                    .build();

            eventRepository.save(event);

            return new ApiResponse<>("Success", "Event created successfully", event);
        } catch (Exception e) {
            throw new LogicException("Failed to create or update event: " +  e.getMessage());
        }
    }


    @Override
    public ApiResponse<Map<String, Object>> getAllEventsPaginated(int page, int size) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to retrieve events");
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Event> eventPage = eventRepository.findAll(pageable);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", eventPage.getContent());
            responseData.put("totalPages", eventPage.getTotalPages());
            responseData.put("totalElements", eventPage.getTotalElements());
            responseData.put("currentPage", eventPage.getNumber());

            return new ApiResponse<>("Success", "Events retrieved successfully", responseData);
        } catch (Exception e) {
            throw new LogicException("Failed to retrieve events: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Event> updateEvent(String eventId, @Valid HandleEventDTO dto) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to update this event");
        }

        // Fetch the existing event by ID
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new LogicException("Event not found"));

        // Map the List<CreateLocalityDTO> to List<Locality>
        List<Locality> updatedLocalities = dto.localities().stream()
                .map(dtoLocality -> Locality.builder()
                        .name(dtoLocality.name())
                        .price(dtoLocality.price())
                        .maxCapacity(dtoLocality.maxCapacity())
                        .build())
                .toList();

        // Validate and upload the event image if needed
        if (dto.eventImageUrl() != null && dto.eventImageUrl().startsWith("data:image/")) {
            try {
                String uploadedEventImageUrl = imageService.uploadImage(dto.eventImageUrl());
                existingEvent.setEventImageUrl(uploadedEventImageUrl);
            } catch (IOException e) {
                throw new LogicException("Failed to upload event image: " + e.getMessage());
            }
        }

        // Validate and upload the localities image if needed
        if (dto.localitiesImageUrl() != null && dto.localitiesImageUrl().startsWith("data:image/")) {
            try {
                String uploadedLocalitiesImageUrl = imageService.uploadImage(dto.localitiesImageUrl());
                existingEvent.setLocalitiesImageUrl(uploadedLocalitiesImageUrl);
            } catch (IOException e) {
                throw new LogicException("Failed to upload localities image: " + e.getMessage());
            }
        }

        // Update the fields from the DTO
        existingEvent.setName(dto.name());
        existingEvent.setCity(dto.city());
        existingEvent.setAddress(dto.address());
        existingEvent.setEventDate(dto.date());
        existingEvent.setTotalAvailablePlaces(dto.totalAvailablePlaces());
        existingEvent.setLocalities(updatedLocalities);
        existingEvent.setEventType(dto.eventType());

        // Save the updated event
        eventRepository.save(existingEvent);

        return new ApiResponse<>("Success", "Event updated", existingEvent);
    }

    @Override
    public ApiResponse<String> deleteEvent(String eventId) {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to delete this event");
        }

        // Check if the coupon exists
        eventRepository.findById(eventId)
                .orElseThrow(() -> new LogicException("Event not found"));

        // Delete the coupon
        eventRepository.deleteById(eventId);
        return new ApiResponse<>("Success", "Event deleted", null);
    }

    @Override
    public ApiResponse<String> deleteAllEvents() {
        // Verify admin access
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to delete all events");
        }

        try {
            eventRepository.deleteAll();
            return new ApiResponse<>("Success", "All events deleted", null);
        } catch (Exception e) {
            throw new LogicException("Failed to delete all events: " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<Map<String, Object>> getAllCouponsPaginated(int page, int size) {

        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to delete all events");
        }
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Coupon> couponPage = couponRepository.findAll(pageable);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", couponPage.getContent());
            responseData.put("totalPages", couponPage.getTotalPages());
            responseData.put("totalElements", couponPage.getTotalElements());
            responseData.put("currentPage", couponPage.getNumber());

            return new ApiResponse<>("Success", "Coupons retrieved successfully", responseData);
        } catch (Exception e) {
            throw new LogicException("Failed to retrieve coupons: " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<UpdateAdminDTO> getAccountInformation(String adminId) {
        String verificationResponse = AdminSecurityUtils.verifyAdminAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AuthorizationException("Not authorized to view admin info");
        }

        try {
            Admin admin = adminRepository.findById(adminId)
                    .orElseThrow(() -> new LogicException("Admin not found"));

            String username = admin.getUsername();
            String email = admin.getEmail();
            UpdateAdminDTO dto = new UpdateAdminDTO(username, email);

            return new ApiResponse<>("Success", "Admin info returned", dto);
        } catch (Exception e) {
            throw new LogicException("Failed to retrieve admin info: " + e.getMessage());
        }
    }


    @Override
    public void generateEventsReport(LocalDateTime startDate, LocalDateTime endDate) {

    }
}
