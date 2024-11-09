package com.uq.jokievents.utils;

import com.uq.jokievents.model.Admin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class AdminSecurityUtils {

    public static String verifyAdminAccessWithId(String id) {
        Admin loggedInAdmin = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedInAdminId = loggedInAdmin.getId();

        // Check if the logged-in client is the same as the one being updated
        if (!loggedInAdminId.equals(id) || !SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return "UNAUTHORIZED";
        }
        return "AUTHORIZED"; // return null if everything is okay (nothing is ever okay)
    }

    public static String verifyAdminAccessWithRole() {
        // Check if the user has the "ADMIN" role
        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            return "UNAUTHORIZED";
        }
        return "AUTHORIZED";
    }
}


