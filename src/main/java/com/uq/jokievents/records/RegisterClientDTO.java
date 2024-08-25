package com.uq.jokievents.records;

import lombok.Data;


@Data
/**
 * Clase para crear un DTO de registro para un Cliente.
 * La anotación Data crear getters, setters, toString(), equals() y hashCode() de una.
 *
 */
public class RegisterClientDTO {

    private String idCard;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String password;
}
