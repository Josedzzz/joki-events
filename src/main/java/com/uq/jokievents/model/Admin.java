package com.uq.jokievents.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "admins")
public class Admin {

    @Id
    private String id;
    private String username;
    private String password;
    private ArrayList<ObjectId> idClients;
    private boolean active;

    // Constructor
    public Admin() {}

    public Admin(String id, String username, String password, ArrayList<ObjectId> idClients) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.idClients = idClients;
        this.active = true;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<ObjectId> getIdClients() {
        return idClients;
    }

    public void setIdClients(ArrayList<ObjectId> idClients) {
        this.idClients = idClients;
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
