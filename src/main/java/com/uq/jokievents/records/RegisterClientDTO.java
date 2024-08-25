package com.uq.jokievents.records;

import lombok.Data;


@Data
public class RegisterClientDTO {

    private String idCard;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String password;
}
