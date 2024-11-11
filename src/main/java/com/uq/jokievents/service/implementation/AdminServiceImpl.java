package com.uq.jokievents.service.implementation;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.uq.jokievents.dtos.*;
import com.uq.jokievents.exceptions.AccountException;
import com.uq.jokievents.exceptions.LogicException;
import com.uq.jokievents.model.*;
import com.uq.jokievents.repository.CouponRepository;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.repository.PurchaseRepository;
import com.uq.jokievents.service.interfaces.*;
import com.uq.jokievents.utils.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.uq.jokievents.repository.AdminRepository;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final AdminRepository adminRepository;
    private final EventRepository eventRepository;
    private final CouponRepository couponRepository;
    private final PurchaseRepository purchaseRepository;
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

            return new ApiTokenResponse<>("Success", "Admin updated", admin, newToken);
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
                    .finalTotalPlaces(dto.totalAvailablePlaces())
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
                        .name(dtoLocality.name())
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

    @Override
    public List<EventReportDTO> generateMonthlyEventReport(int month, int year) {
        // Fetch purchases within the specified month and year based on purchaseDate
        List<Purchase> purchases = purchaseRepository.findByPurchaseDateBetween(
                LocalDateTime.of(year, month, 1, 0, 0),
                LocalDateTime.of(year, month, 1, 0, 0).plusMonths(1)
        );

        Map<String, EventReportDTO> eventReports = new HashMap<>();

        for (Purchase purchase : purchases) {
            for (LocalityOrder localityOrder : purchase.getPurchasedItems()) {
                String eventId = localityOrder.getEventId();

                Optional<Event> optionalEvent = eventRepository.findById(eventId);
                if (optionalEvent.isEmpty()) {
                    continue;
                }
                Event event = optionalEvent.get();
                // Retrieve or initialize the EventReportDTO
                EventReportDTO currentReport = eventReports.get(eventId);

                List<LocalityStats> updatedLocalityStats;
                BigDecimal updatedRevenue;

                if (currentReport == null) {
                    // Create new LocalityStats list if event report doesn't exist yet
                    updatedLocalityStats = new ArrayList<>();
                    updatedRevenue = purchase.getTotalAmount();
                } else {
                    // Copy existing localityStats and revenue
                    updatedLocalityStats = new ArrayList<>(currentReport.localityStats());
                    updatedRevenue = currentReport.totalRevenue().add(purchase.getTotalAmount());
                }

                // Update locality statistics
                Optional<LocalityStats> existingStats = updatedLocalityStats.stream()
                        .filter(stats -> stats.getLocalityName().equals(localityOrder.getLocalityName()))
                        .findFirst();

                if (existingStats.isPresent()) {
                    LocalityStats stats = existingStats.get();
                    int updatedTicketsSold = stats.getTicketsSold() + localityOrder.getNumTicketsSelected();
                    double updatedLocalityRevenue = stats.getLocalityRevenue().add(BigDecimal.valueOf(localityOrder.getTotalPaymentAmount())).toBigInteger().doubleValue();
                    int totalTickets = event.getLocalities().stream()
                            .filter(loc -> loc.getName().equals(localityOrder.getLocalityName()))
                            .map(Locality::getMaxCapacity)
                            .findFirst()
                            .orElse(0);
                    double soldPercentage = (double) updatedTicketsSold / totalTickets * 100;

                    updatedLocalityStats.remove(stats); // Remove old entry to replace with updated stats
                    updatedLocalityStats.add(new LocalityStats(
                            stats.getLocalityName(), updatedTicketsSold, totalTickets, soldPercentage, BigDecimal.valueOf(updatedLocalityRevenue)
                    ));
                } else {
                    int totalTickets = event.getLocalities().stream()
                            .filter(loc -> loc.getName().equals(localityOrder.getLocalityName()))
                            .map(Locality::getMaxCapacity)
                            .findFirst()
                            .orElse(0);
                    double soldPercentage = (double) localityOrder.getNumTicketsSelected() / totalTickets * 100;

                    updatedLocalityStats.add(new LocalityStats(
                            localityOrder.getLocalityName(), localityOrder.getNumTicketsSelected(),
                            totalTickets, soldPercentage, BigDecimal.valueOf(localityOrder.getTotalPaymentAmount())
                    ));
                }

                // Update the map with the new EventReportDTO record
                eventReports.put(eventId, new EventReportDTO(
                        eventId, event.getName(), event.getCity() , event.getAddress(), updatedRevenue, updatedLocalityStats
                ));
            }
        }
        return new ArrayList<>(eventReports.values());
    }

    public ByteArrayInputStream generateMonthlyEventReportPdf(int month, int year) {
        List<EventReportDTO> reportData = generateMonthlyEventReport(month, year);

        if (reportData.isEmpty()) throw new LogicException("No events to generate a report");

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(out); PdfDocument pdfDoc = new PdfDocument(writer)) {
            Document document = new Document(pdfDoc);

            // Title and report date
            document.add(new Paragraph("Monthly Event Report Joki Eventos")
                    .setBold().setFontSize(16));
            document.add(new Paragraph("Report Date: " + month + "/" + year));
            document.add(new Paragraph("Total revenue: $" + reportData.get(0).totalRevenue()));

            for (EventReportDTO eventReport : reportData) {

                List<LocalityStats> localityStatsList = eventReport.localityStats();
                LocalityStats localityStats = localityStatsList.get(0);
                if (localityStats == null) throw new LogicException("The event report has no locality stats, grave error");
                // Event title
                document.add(new Paragraph("Event: " + eventReport.eventName())
                        .setBold().setFontSize(14));
                document.add(new Paragraph("City: " + eventReport.eventCity()));
                document.add(new Paragraph("Address: " + eventReport.address()));
                document.add(new Paragraph("Event Revenue: $" + localityStats.getLocalityRevenue()));

                // Table for locality statistics
                Table table = new Table(new float[]{4, 2, 2, 2});
                table.addHeaderCell("Locality");
                table.addHeaderCell("Tickets Sold");
                table.addHeaderCell("Total Tickets");
                table.addHeaderCell("Sold Percentage (%)");

                for (LocalityStats locality : eventReport.localityStats()) {
                    table.addCell(locality.getLocalityName());
                    table.addCell(String.valueOf(locality.getTicketsSold()));
                    table.addCell(String.valueOf(locality.getTotalTickets()));
                    table.addCell(String.format("%.2f", locality.getSoldPercentage()));
                }
                document.add(table);
                document.add(new Paragraph("\n")); // Add space between events
            }
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
}
