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
    private boolean availablePurchase;
    private String imageUrl;

    // Constructor
    public Event() {}

    public Event(String id, String name, String address, String city, LocalDateTime eventDate, boolean availablePurchase, String imageUrl, String imapeDistributionLocality) {
        this.id = id;
        this.idDistributionLocality = new ArrayList<>();
        this.name = name;
        this.address = address;
        this.city = city;
        this.eventDate = eventDate;
        this.availablePurchase = availablePurchase;
        this.imageUrl = imageUrl;
    }

}
