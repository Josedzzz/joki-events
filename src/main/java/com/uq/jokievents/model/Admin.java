package com.uq.jokievents.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Document(collection = "admins")
public class Admin {

    @Id
    private String id;
    private String username;
    private String password;
    private ArrayList<String> idClients;

    // Constructor
    public Admin() {}

    public Admin(String id, String username, String password, ArrayList<String> idClients) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.idClients = idClients;
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

    public ArrayList<String> getIdClients() {
        return idClients;
    }

    public void setIdClients(ArrayList<String> idClients) {
        this.idClients = idClients;
    }

}
