package com.uq.jokievents.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

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

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ObjectId getIdDistributionLocality() {
        return idDistributionLocality;
    }

    public void setIdDistributionLocality(ObjectId idDistributionLocality) {
        this.idDistributionLocality = idDistributionLocality;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public boolean isAvailablePurchase() {
        return availablePurchase;
    }

    public void setAvailablePurchase(boolean availablePurchase) {
        this.availablePurchase = availablePurchase;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImapeDistributionLocality() {
        return imapeDistributionLocality;
    }

    public void setImapeDistributionLocality(String imapeDistributionLocality) {
        this.imapeDistributionLocality = imapeDistributionLocality;
    }
}
