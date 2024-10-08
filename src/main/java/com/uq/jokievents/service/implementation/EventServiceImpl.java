package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.HandleEventDTO;
import com.uq.jokievents.dtos.ReportEventDTO;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.Locality;
import com.uq.jokievents.model.enums.EventType;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.ImageService;
import com.uq.jokievents.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;


    @Override
    public ResponseEntity<?> getAllEventsPaginated(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Event> eventPage = eventRepository.findAll(pageable);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", eventPage.getContent());
            responseData.put("totalPages", eventPage.getTotalPages());
            responseData.put("totalElements", eventPage.getTotalElements());
            responseData.put("currentPage", eventPage.getNumber());

            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Success", "Events retrieved successfully", responseData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = new ApiResponse<>("Error", "Failed to retrieve events", null);
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> addEvent(HandleEventDTO dto) {
        try {
            String imageUrl = imageService.uploadImage(dto.eventImageUrl());
            String localitiesUrl = imageService.uploadImage(dto.localitiesImageUrl());
            Event event = Event.builder()
                    .name(dto.name())
                    .address(dto.address())
                    .city(dto.city())
                    .eventDate(dto.date())
                    .availableForPurchase(true)  // El negro? Mi color. Si es, jeje
                    .localities(dto.localities().stream().map(localityDTO ->
                            // Locality has a JsonIgnore on the id parameter
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

            ApiResponse<Event> response = new ApiResponse<>("Success", "Event created successfully", event);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to create or update event event", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> updateEvent(String eventId, HandleEventDTO dto) {
        // Fetch the existing event by ID
        Optional<Event> existingEventOpt = eventRepository.findById(eventId);
        if (existingEventOpt.isEmpty()) {
            ApiResponse<Event> response = new ApiResponse<>("Success", "Event not found", null);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        // Get the existing event object
        Event existingEvent = existingEventOpt.get();

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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload event image.");
            }
        }

        // Validate and upload the localities image if needed
        if (dto.localitiesImageUrl() != null && dto.localitiesImageUrl().startsWith("data:image/")) {
            try {
                String uploadedLocalitiesImageUrl = imageService.uploadImage(dto.localitiesImageUrl());
                existingEvent.setLocalitiesImageUrl(uploadedLocalitiesImageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload localities image.");
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

        ApiResponse<Event> response = new ApiResponse<>("Success", "Event created successfully", existingEvent);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public Optional<Event> getEventById(String eventId) {
        return eventRepository.findById(eventId);
    }

    @Override
    public void saveEvent(Event event) {
        eventRepository.save(event);
    }

    @Override
    public void deleteEventById(String eventId) {
        eventRepository.deleteById(eventId);
    }

    @Override
    public void deleteAllEvents(){
        eventRepository.deleteAll();
    }

    @Override
    public ResponseEntity<?> filterEventsAfterCertainDate(String date, int page, int size) {
        try {
            // Parse the date
            LocalDateTime dateFilter = LocalDateTime.parse(date);

            // Fetch events after the specified date
            List<Event> events = eventRepository.findByEventDateAfter(dateFilter);

            if (events.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>("No Events", "No events found after this date", null), HttpStatus.OK);
            }

            // Paginate the result
            int totalElements = events.size();  // Total number of events found
            int totalPages = (int) Math.ceil((double) totalElements / size);  // Calculate total number of pages
            int startIndex = page * size;  // Calculate the start index for the page
            int endIndex = Math.min(startIndex + size, totalElements);  // Calculate the end index for the page

            if (startIndex >= totalElements) {
                return new ResponseEntity<>(new ApiResponse<>("Success", "No events found for the specified page", List.of()), HttpStatus.OK);
            }

            // Get the paginated sublist
            List<Event> paginatedEvents = events.subList(startIndex, endIndex);

            // Prepare pagination metadata
            Map<String, Object> paginationData = new HashMap<>();
            paginationData.put("totalPages", totalPages);
            paginationData.put("currentPage", page);
            paginationData.put("totalElements", totalElements);
            paginationData.put("content", paginatedEvents);  // The paginated events for the current page

            // Return the paginated response
            return new ResponseEntity<>(new ApiResponse<>("Success", "Events found", paginationData), HttpStatus.OK);

        } catch (DateTimeParseException e) {
            // Handle invalid date format
            return new ResponseEntity<>(new ApiResponse<>("Error", "Invalid date format. Expected format: yyyy-MM-ddTHH:mm:ss", null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Error", "An error occurred", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<?> filterEventsBetweenDates(String startDate, String endDate, int page, int size) {
        try {
            // Parse the dates
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);

            if (end.isBefore(start)) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "End date must be after start date", null), HttpStatus.BAD_REQUEST);
            }

            // Fetch events between the two dates
            List<Event> events = eventRepository.findByEventDateBetween(start, end);

            if (events.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>("No Events", "No events found between these dates", null), HttpStatus.OK);
            }

            // Paginate the result
            int totalElements = events.size();  // Total number of events found
            int totalPages = (int) Math.ceil((double) totalElements / size);  // Calculate total number of pages
            int startIndex = page * size;  // Calculate the start index for the page
            int endIndex = Math.min(startIndex + size, totalElements);  // Calculate the end index for the page

            if (startIndex >= totalElements) {
                return new ResponseEntity<>(new ApiResponse<>("Success", "No events found for the specified page", List.of()), HttpStatus.OK);
            }

            // Get the paginated sublist
            List<Event> paginatedEvents = events.subList(startIndex, endIndex);

            // Prepare pagination metadata
            Map<String, Object> paginationData = new HashMap<>();
            paginationData.put("totalPages", totalPages);
            paginationData.put("currentPage", page);
            paginationData.put("totalElements", totalElements);
            paginationData.put("content", paginatedEvents);  // The paginated events for the current page

            // Return the paginated response
            return new ResponseEntity<>(new ApiResponse<>("Success", "Events found", paginationData), HttpStatus.OK);

        } catch (DateTimeParseException e) {
            // Handle invalid date format
            return new ResponseEntity<>(new ApiResponse<>("Error", "Invalid date format. Expected format: yyyy-MM-ddTHH:mm:ss", null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Error", "An error occurred", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public Optional<Event> findByEventById(String eventId) {
        return eventRepository.findById(eventId);
    }

    @Override
    public Optional<Event> findEventByLocalityName(String localityName) {
        return eventRepository.findByLocalitiesName(localityName);
    }

    /**
     * If eventName is null. If it is, it will not filter by event name, allowing for all event names to pass.
     * If city is null, it won’t filter by city.
     * If startDate is null, it won’t filter out any events based on the start date.
     * If endDate is null, it won’t filter out any events based on the end date.
     * Both date checks ensure that the event date is not null before comparing.
     * If eventType is null, it won’t filter based on event type.
     * If searching with null parameters, will show all the events
     * @param eventName String
     * @param city String
     * @param startDate LocalDateTime
     * @param endDate LocalDateTime
     * @param eventType EventType
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<?> searchEvent(String eventName, String city, LocalDateTime startDate, LocalDateTime endDate, EventType eventType, int page, int size) {
        try {
            // Fetch all events from the repository
            List<Event> allEvents = eventRepository.findAll();

            // Use stream to filter events based on the criteria
            List<Event> filteredEvents = allEvents.stream()
                    .filter(event ->
                            (eventName == null || eventName.isEmpty() || (event.getName() != null && event.getName().toLowerCase().contains(eventName.toLowerCase()))) // Flexible event name match
                                    && (city == null || city.isEmpty() || (event.getCity() != null && event.getCity().toLowerCase().contains(city.toLowerCase()))) // Flexible city match
                                    && (startDate == null || (event.getEventDate() != null && !event.getEventDate().isBefore(startDate))) // Correct start date filter
                                    && (endDate == null || (event.getEventDate() != null && !event.getEventDate().isAfter(endDate))) // Correct end date filter
                                    && (eventType == null || eventType.equals(event.getEventType())) // Strict event type match
                    )
                    .toList(); // Collect the filtered events into a list

            // Calculate pagination
            int totalElements = filteredEvents.size();  // Total number of filtered events
            int totalPages = (int) Math.ceil((double) totalElements / size);  // Total number of pages
            int start = page * size;  // Start index
            int end = Math.min(start + size, totalElements);  // End index

            // Ensure the requested page is within bounds
            if (start >= totalElements) {
                return new ResponseEntity<>(new ApiResponse<>("Success", "No events found for the specified page", List.of()), HttpStatus.OK);
            }

            // Get the sublist for the current page
            List<Event> paginatedEvents = filteredEvents.subList(start, end);

            // Prepare pagination metadata
            Map<String, Object> paginationData = new HashMap<>();
            paginationData.put("totalPages", totalPages);
            paginationData.put("currentPage", page);
            paginationData.put("totalElements", totalElements);
            paginationData.put("content", paginatedEvents);  // The paginated events for the current page

            // Return response
            return new ResponseEntity<>(new ApiResponse<>("Success", "Events found", paginationData), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Error", "An error occurred", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @Override
    public ResponseEntity<?> generateEventsReport(LocalDateTime startDate, LocalDateTime endDate) {
        // Fetch events within the date range or all if no range is provided
        List<Event> events = (startDate != null && endDate != null)
                ? eventRepository.findByEventDateBetween(startDate, endDate)
                : eventRepository.findAll();
        List<ReportEventDTO> reportEvents = events.stream()
                .map(event -> new ReportEventDTO(
                        event.getId(),
                        event.getName(),
                        event.getCity(),
                        event.getEventDate(),
                        event.getTotalAvailablePlaces(),
                        event.getTotalAvailablePlaces() - calculateOccupancy(event.getLocalities()),
                        calculateOccupancy(event.getLocalities()),
                        event.getEventType(),
                        calculatePercentageSold(event)
                ))
                .toList();
        // Map each Event to ReportEventDTO with additional stats
        return new ResponseEntity<>(new ApiResponse<>("Success", "Report generated", reportEvents), HttpStatus.OK);
    }


    // Method to calculate current occupancy across all localities
    private int calculateOccupancy(List<Locality> localities) {
        return localities.stream()
                .mapToInt(Locality::getCurrentOccupancy)
                .sum();
    }

    // Method to calculate the percentage of sold tickets
    private double calculatePercentageSold(Event event) {
        int totalCapacity = event.getTotalAvailablePlaces();
        int occupancy = calculateOccupancy(event.getLocalities());
        return (totalCapacity > 0) ? (double) occupancy / totalCapacity * 100 : 0;
    }
}

