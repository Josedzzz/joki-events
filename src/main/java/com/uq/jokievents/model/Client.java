package com.uq.jokievents.model;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import com.uq.jokievents.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@Document(collection = "clients")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Client implements UserDetails {

    @Id
    private String id; // Mongo will take care of it.
    private String idCard; // Kind of a country given identifier.
    private String name;
    private String direction; // Deberías ser "address" pero me da miedo cambiarlo.
    private String phoneNumber;
    private String email;
    private String password;
    private ArrayList<ObjectId> idCoupons; // TODO Implementar la clase CouponID (¿Cómo sé qué no eres uno de ellos?)
    private ObjectId idShoppingCart;
    private boolean active;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return "EVER SINCE I MET YOU";
    }
}
