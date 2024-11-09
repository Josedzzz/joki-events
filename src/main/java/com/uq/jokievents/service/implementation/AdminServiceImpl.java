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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${image.not.found}")
    private String imageNotFound;


    @Override
    public ApiTokenResponse<Object> updateAdmin(String adminId, UpdateAdminDTO dto) {
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
    public ApiResponse<UpdateAdminDTO> getAccountInformation(String adminId) {
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
    public ApiResponse<String> deleteAdminAccount(String adminId) {
        try {
            // Fetch the admin record
            Admin admin = adminRepository.findById(adminId).orElseThrow(() ->
                    new AccountException("Admin not found"));

            // Deactivate the account
            admin.setActive(false);
            adminRepository.save(admin);
            return new ApiResponse<>("Success", "Admin deleted", null);
        } catch (AccountException e) {
            // Rethrow as a custom exception for the controller to handle
            throw new AccountException("Failed to deactivate admin account, serverside: " + e.getMessage());
        }
    }


    @Override
    public ApiResponse<Coupon> createCoupon(CreateCouponDTO dto) {
        // Check if a coupon with the same name already exists
        Optional<Coupon> existingCoupon = couponRepository.findByName(dto.name());
        if (existingCoupon.isPresent()) {
            throw new LogicException("Coupon with the same name already exists");
        }

        // Create and save the new coupon
        Coupon coupon = new Coupon();
        coupon.setName(dto.name());
        coupon.setDiscountPercent(dto.discount());
        coupon.setExpirationDate(dto.expirationDate());
        coupon.setMinPurchaseAmount(dto.minPurchaseAmount());
        coupon.setCouponType(dto.couponType());
        couponRepository.save(coupon);

        // Return success response
        return new ApiResponse<>("Success", "Coupon creation done", coupon);
    }


    @Override
    public ApiResponse<Coupon> updateCoupon(String couponId, UpdateCouponDTO dto) {
        // Retrieve and update the coupon
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new LogicException("Coupon not found"));

        // Update the coupon fields
        coupon.setDiscountPercent(dto.discount());
        coupon.setExpirationDate(dto.expirationDate());
        coupon.setMinPurchaseAmount(dto.minPurchaseAmount());

        // Save and return updated coupon
        couponRepository.save(coupon);
        return new ApiResponse<>("Success", "Coupon updated", coupon);
    }

    @Override
    public ApiResponse<Map<String, Object>> getAllCouponsPaginated(int page, int size) {
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
    public ApiResponse<String> deleteCoupon(String couponId) {

        // Check if the coupon exists
        couponRepository.findById(couponId)
                .orElseThrow(() -> new LogicException("Coupon not found"));

        // Delete the coupon
        couponRepository.deleteById(couponId);
        return new ApiResponse<>("Success", "Coupon deleted", null);
    }


    @Override
    public ApiResponse<String> deleteAllCoupons() {
        try {
            couponRepository.deleteAll();
            return new ApiResponse<>("Success", "All coupons deleted", null);
        } catch (Exception e) {
            throw new LogicException("Failed to delete all coupons");
        }
    }


    @Override
    public ApiResponse<Event> addEvent(HandleEventDTO dto) {
        try {
            checkEventInSitu(dto);
            String eventUrl = imageNotFound;
            String localitiesUrl = imageNotFound;
            boolean bothImagesEmpty = dto.eventImageUrl().isEmpty() && dto.localitiesImageUrl().isEmpty();
            if (!bothImagesEmpty) {
                // XD
                eventUrl = imageService.uploadImage(dto.eventImageUrl());
                localitiesUrl = imageService.uploadImage(dto.localitiesImageUrl());
            }

            // get localities with some validations
            List<Locality> newEventLocalities = getLocalities(dto);

            Event event = Event.builder()
                    .name(dto.name())
                    .address(dto.address())
                    .city(dto.city())
                    .eventDate(dto.date())
                    .availableForPurchase(true)  // Event available for purchase
                    .localities(newEventLocalities)
                    .totalAvailablePlaces(dto.totalAvailablePlaces())
                    .eventImageUrl(eventUrl)
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
    public ApiResponse<Event> updateEvent(String eventId,HandleEventDTO dto) {
        // Fetch the existing event by ID
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new LogicException("Event not found"));

        // checking the images sent to the request is not the same image not found one as this would fill unnecesarilly the firebase repository
        String eventImageUrl = dto.eventImageUrl();
        String localitiesImageUrl = dto.localitiesImageUrl();

        if (!(eventImageUrl.equals(imageNotFound) && localitiesImageUrl.equals(imageNotFound))) {
            // if both are not the image not found (unique case possible)
            // Validate and upload the event image if needed
            // Validate and upload the localities image if needed
            if (dto.eventImageUrl().startsWith("data:image/")) {
                try {
                    String uploadedEventImageUrl = imageService.uploadImage(dto.eventImageUrl());
                    existingEvent.setEventImageUrl(uploadedEventImageUrl);
                } catch (IOException e) {
                    throw new LogicException("Failed to upload event image: " + e.getMessage());
                }
            }

            if (dto.localitiesImageUrl() != null && dto.localitiesImageUrl().startsWith("data:image/")) {
                try {
                    String uploadedLocalitiesImageUrl = imageService.uploadImage(dto.localitiesImageUrl());
                    existingEvent.setLocalitiesImageUrl(uploadedLocalitiesImageUrl);
                } catch (IOException e) {
                    throw new LogicException("Failed to upload localities image: " + e.getMessage());
                }
            }
        }

        checkEventInSitu(dto); // two not used conditions but well fuck
        List<Locality> updatedLocalities = getLocalities(dto);

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

    private void checkEventInSitu(HandleEventDTO dto) {
        LocalDateTime eventDate = dto.date();
        String address = dto.address();
        String city = dto.city();
        String eventImage = dto.eventImageUrl();
        String localitiesImage = dto.localitiesImageUrl();
        if (eventRepository.existsByEventDate(eventDate) && eventRepository.existsByAddress(address) && eventRepository.existsByCity(city)) {
            throw new LogicException("Event at that time in the same address in the same city exists");
        }
        if (!eventImage.isEmpty() && localitiesImage.isEmpty()) {
            throw new LogicException("Please add a localities image for the event");
        }
        if (!localitiesImage.isEmpty() && eventImage.isEmpty()) {
            throw new LogicException("Please add an event image for the localities");
        }
    }

    private static List<Locality> getLocalities(HandleEventDTO dto) {
        int totalLocalitiesCapacity = dto.localities().stream().mapToInt(CreateLocalityDTO::maxCapacity).sum();

        // Check if total locality capacity matches the event's total available places
        if (totalLocalitiesCapacity != dto.totalAvailablePlaces()) {
            throw new LogicException("The sum of localities' capacities does not match the event's total available places.");
        }

        // Map the List<CreateLocalityDTO> to List<Locality>
        return dto.localities().stream()
                .map(dtoLocality -> Locality.builder()
                        .name(dtoLocality.localityName())
                        .price(dtoLocality.price())
                        .maxCapacity(dtoLocality.maxCapacity())
                        .build())
                .toList();
    }

    @Override
    public ApiResponse<String> deleteEvent(String eventId) {
        // Check if the coupon exists
        eventRepository.findById(eventId)
                .orElseThrow(() -> new LogicException("Event not found"));

        // Delete the coupon
        eventRepository.deleteById(eventId);
        return new ApiResponse<>("Success", "Event deleted", null);
    }

    @Override
    public ApiResponse<String> deleteAllEvents() {
        try {
            eventRepository.deleteAll();
            return new ApiResponse<>("Success", "All events deleted", null);
        } catch (Exception e) {
            throw new LogicException("Failed to delete all events: " + e.getMessage());
        }
    }

    @Override
    public void generateEventsReport(LocalDateTime startDate, LocalDateTime endDate) {

    }

    @Override
    public ApiTokenResponse<Map<String, Object>> getAllAdmins() {

        try {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Admin> couponPage = adminRepository.findAll(pageable);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", couponPage.getContent());
            responseData.put("totalPages", couponPage.getTotalPages());
            responseData.put("totalElements", couponPage.getTotalElements());
            responseData.put("currentPage", couponPage.getNumber());

            return new ApiTokenResponse<>("Success", "Admins retrieved successfully", responseData, null);
        } catch (Exception e) {
            throw new LogicException("Failed to retrieve admins: " + e.getMessage());
        }
    }
}
