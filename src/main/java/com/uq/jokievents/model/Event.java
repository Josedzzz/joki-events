package com.uq.jokievents.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "events")
public class Event {

    @Id
    private String id;
    private List<ObjectId> idDistributionLocality;
    private String name;
    private String address;
    private String city;
    private LocalDateTime eventDate;
    private boolean availableForPurchase;
    private int totalAvailableSeatsForPurchase;
    private String imageUrl; //TODO implement images asap.

    // Constructor
    public Event() {}

    public Event(String id, List<ObjectId> idDistributionLocality, String name, String address, String city, LocalDateTime eventDate, boolean availableForPurchase, int totalAvailableSeatsForPurchase, String imageUrl) {
        this.id = id;
        this.idDistributionLocality = idDistributionLocality;
        this.name = name;
        this.address = address;
        this.city = city;
        this.eventDate = eventDate;
        this.availableForPurchase = availableForPurchase;
        this.totalAvailableSeatsForPurchase = totalAvailableSeatsForPurchase;
        this.imageUrl = imageUrl;
    }
}
