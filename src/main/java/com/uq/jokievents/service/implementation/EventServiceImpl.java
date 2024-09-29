package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.HandleEventDTO;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;

    /**
     * Gets an event by its id
     *
     * @param id the identifier of the event
     * @return an Optional containing the event if found, empty if not
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<Event> event = eventRepository.findById(id);
            if (event.isPresent()) {
                return new ResponseEntity<>(event.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed event request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves a new event or updates an existing in the db
     *
     * @param event the event object to be saver or updated
     * @return the saved or updated event object
     */
    public ResponseEntity<?> create(Event event) {
        try {
            Event createdEvent = eventRepository.save(event);
            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifierof the event to be update
     * @param event the updated event object
     * @return a ResponseEntity containing the updated event object and an HTTP status
     */
    public ResponseEntity<?> update(String id, Event event) {
        try {
            Optional<Event> existingEvent = eventRepository.findById(id);
            if (existingEvent.isPresent()) {
                event.setId(id);
                Event updatedEvent = eventRepository.save(event);
                return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a event from the db using its id
     *
     * @param id the identifier of the event object
     */
    public ResponseEntity<?> deleteById(String id) {
        try {
            Optional<Event> existingEvent = eventRepository.findById(id);
            if (existingEvent.isPresent()) {
                eventRepository.deleteById(id);
                return new ResponseEntity<>("Event deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Event not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete event", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
                    .availableForPurchase(true)  // El negro? Mi color. Si es jeje
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

            ApiResponse<Event> response = new ApiResponse<>("Success", "Event created successfully", event);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to create event", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Optional<Event> getEventById(String eventId) {
        return eventRepository.findById(eventId);
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
    public ResponseEntity<?> filterEventsByEventType(EventType eventType) {
        List<Event> events = eventRepository.findByEventType(eventType);
        if (events.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse<>("No Events", "No events found for this type", null), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>("Success", "Events found", events), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> filterEventsAfterCertainDate(String date) {
        LocalDateTime dateFilter = LocalDateTime.parse(date);
        List<Event> events = eventRepository.findByEventDateAfter(dateFilter);
        if (events.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse<>("No Events", "No events found after this date", null), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse<>("Success", "Events found", events), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> filterEventsBetweenDates(String startDate, String endDate) {
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

            return new ResponseEntity<>(new ApiResponse<>("Success", "Events found", events), HttpStatus.OK);

        } catch (DateTimeParseException e) {
            // Handle invalid date format
            return new ResponseEntity<>(new ApiResponse<>("Error", "Invalid date format. Expected format: yyyy-MM-ddTHH:mm:ss", null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiResponse<>("Error", "An error occurred", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

