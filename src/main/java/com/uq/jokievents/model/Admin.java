package com.uq.jokievents.model;

import lombok.Data;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
@Document(collection = "admins")
public class Admin {

    @Id
    private String id;
    private String email;
    private String username;
    private String password;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private ArrayList<ObjectId> idClients;
    private boolean active;

    // Constructor
    public Admin() {}

    public Admin(String id, String email,String username, String password, ArrayList<ObjectId> idClients) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.idClients = idClients;
        this.active = true;
    }

}
