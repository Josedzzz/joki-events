package com.uq.jokievents.dtos;

// As sent by google, these will not be empty
public record GoogleUserDTO(
        String idToken,
        String email,
        String name
) {
}
