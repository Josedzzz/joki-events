package com.uq.jokievents.model;

import com.uq.jokievents.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
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
    private String eventImageBase64;
    private String eventImageUrl; // For the URL after upload
    private EventType eventType;
}
