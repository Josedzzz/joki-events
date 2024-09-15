package com.uq.jokievents.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Future;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "events")
public class Event {

    @Id
    private String id;
    private List<Locality> localities; // TODO Explain why I made this change to the team!
    private String name;
    private String address;
    private String city;
    private LocalDateTime eventDate;
    private boolean availableForPurchase;
    private int totalAvailablePlaces;
    private String eventImageUrl; //TODO implement images asap.

    // Constructor
    public Event() {}

    public Event(List<Locality> localities, String name, String address, String city, LocalDateTime eventDate, boolean availableForPurchase, int totalAvailableSeatsForPurchase, String eventImageUrl) {
        this.localities = localities;
        this.name = name;
        this.address = address;
        this.city = city;
        this.eventDate = eventDate;
        this.availableForPurchase = availableForPurchase;
        this.totalAvailablePlaces = totalAvailableSeatsForPurchase;
        this.eventImageUrl = eventImageUrl;
    }

    // Method made by IntelliJ usen in AdminServiceImpl
    public <R> Event(@NotBlank(message = "Name is required") String name, @NotBlank(message = "City is required") String city, @NotBlank(message = "Address is required") String address, @Future(message = "Date must be in the future") LocalDateTime date, @Min(value = 1, message = "Total available places must be at least 1") int i, @NotBlank(message = "URL is required") String s, R collect) {
    }
}
