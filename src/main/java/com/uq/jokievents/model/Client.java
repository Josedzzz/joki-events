package com.uq.jokievents.model;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.uq.jokievents.model.enums.Role;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Document(collection = "clients")
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Client implements UserDetails {

    @Id
    private String id;
    private String idCard;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String password;
    private String idShoppingCart; // This is a "pointer" to the shopping car in the database.
    @Transient private List<Purchase> purchaseHistory;
    private ArrayList<String> listOfUsedCoupons;
    private boolean active;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    @Builder.Default
    private Role role = Role.CLIENT;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }
}
