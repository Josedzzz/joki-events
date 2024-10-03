package com.uq.jokievents.model;

import com.uq.jokievents.model.enums.EventType;
import jakarta.validation.constraints.Future;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
public class Event {

    @Id
    private String id;
    private String name;
    private String address;
    private String city;
    private LocalDateTime eventDate;
    private boolean availableForPurchase;
    private List<Locality> localities;
    private int totalAvailablePlaces;
    private String eventImageUrl; // For the URL after upload
    private String localitiesImageUrl;
    private EventType eventType;
}
