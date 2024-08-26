package com.uq.jokievents.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "events")
public class Event {

    @Id
    private String id;
    private ObjectId idDistributionLocality;
    private String name;
    private String address;
    private String city;
    private Date eventDate;
    private boolean availablePurchase;
    private String imageUrl;
    private String imapeDistributionLocality;

    // Constructor
    public Event() {}

    public Event(String id, ObjectId idDistributionLocality, String name, String address, String city, Date eventDate, boolean availablePurchase, String imageUrl, String imapeDistributionLocality) {
        this.id = id;
        this.idDistributionLocality = idDistributionLocality;
        this.name = name;
        this.address = address;
        this.city = city;
        this.eventDate = eventDate;
        this.availablePurchase = availablePurchase;
        this.imageUrl = imageUrl;
        this.imapeDistributionLocality = imapeDistributionLocality;
    }

}
