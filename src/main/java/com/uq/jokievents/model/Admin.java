package com.uq.jokievents.model;

import com.uq.jokievents.model.enums.Role;
import lombok.Data;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Document(collection = "admins")
public class Admin implements UserDetails {

    @Id
    private String id;
    private String email;
    private String username;
    private String password;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private ArrayList<ObjectId> idClients;
    private boolean active;
    private Role role = Role.ADMIN;

    // Constructor
    public Admin() {}

    public Admin(String email,String username, String password, ArrayList<ObjectId> idClients) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.idClients = idClients;
        this.active = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }
}
