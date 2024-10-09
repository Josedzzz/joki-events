package com.uq.jokievents.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.uq.jokievents.model.enums.EventType;
import jakarta.validation.constraints.Future;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Document(collection = "events")
// TODO Add an int of totalUsedDiscount. Should be easy.
public class Event {

    @Id
    private String id;
    private String name;
    private String address;
    private String city;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime eventDate;
    private boolean availableForPurchase;
    private List<Locality> localities;
    private int totalAvailablePlaces;
    private String eventImageUrl;
    private String localitiesImageUrl;
    private EventType eventType;

    public Locality getLocalities(String localityName) {
        for (Locality locality : localities) {
            if (locality.getName().equals(localityName)) {
                return locality;
            }
        }
        return null;
    }
}
