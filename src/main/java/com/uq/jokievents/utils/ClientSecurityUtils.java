package com.uq.jokievents.utils;

import com.uq.jokievents.model.Client;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class ClientSecurityUtils {

    public static ResponseEntity<?> verifyClientAccess(String id) {
        Client loggedInClient = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedInClientId = loggedInClient.getId();

        // Check if the logged-in client is the same as the one being updated
        if (!loggedInClientId.equals(id) || !SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("CLIENT"))) {
            ApiResponse<String> response = new ApiResponse<>("Error", "You are not authorized to access this client", null);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        return null; // return null if everything is okay (nothing is ever okay)
    }
}
