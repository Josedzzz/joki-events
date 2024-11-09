package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.ReportEventDTO;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.Locality;
import com.uq.jokievents.model.enums.EventType;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.ImageService;
import com.uq.jokievents.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ImageService imageService;


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
        return null;
    }


    // TODO Add eventId to generate reports of a single event
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
                        event.getEventDate().toString(),
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

