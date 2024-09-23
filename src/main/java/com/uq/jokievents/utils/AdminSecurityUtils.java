package com.uq.jokievents.utils;

import com.uq.jokievents.model.Admin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;



public class AdminSecurityUtils {

    public static ResponseEntity<?> verifyAdminAccessWithId(String id) {
        Admin loggedInAdmin = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedInAdminId = loggedInAdmin.getId();

        // Check if the logged-in client is the same as the one being updated
        if (!loggedInAdminId.equals(id) || !SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            ApiResponse<String> response = new ApiResponse<>("Error", "You are not authorized to access", null);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        return null; // return null if everything is okay (nothing is ever okay)
    }

    public static ResponseEntity<?> verifyAdminAccessWithEmail(String email) {
        Admin loggedInAdmin = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedInAdminEmail = loggedInAdmin.getEmail();

        if (!loggedInAdminEmail.equals(email) ||!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ADMIN")) ) {
            ApiResponse<String> response = new ApiResponse<>("Error", "You are not authorized to access this action.", null);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        return null;
    }

    public static ResponseEntity<?> verifyAdminAccessWithRole() {
        // Check if the user has the "ADMIN" role
        if (!SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("ADMIN"))) {
            ApiResponse<String> response = new ApiResponse<>("Error", "You are not authorized to create coupons", null);
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        return null;
    }
}


