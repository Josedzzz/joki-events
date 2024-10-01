package com.uq.jokievents.model;

import com.uq.jokievents.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class Admin implements UserDetails {

    @Id
    private String id;
    private String email;
    private String username;
    private String password;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private boolean active;
    private final Role role = Role.ADMIN;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }
}
