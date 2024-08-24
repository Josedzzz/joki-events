package com.uq.jokievents.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ClientDto {

    // ¿Cuándo realmente usar los Data Transfer Object?
    private String name;
    private String direction;
    private String phoneNumber;
    private String email;

    public ClientDto(String name, String direction, String phoneNumber, String email) {
        this.name = name;
        this.direction = direction;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }


}
