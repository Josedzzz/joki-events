package com.uq.jokievents.utils;

import com.uq.jokievents.model.Client;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class ClientSecurityUtils {

    public static String verifyClientAccessWithId(String id) {
        Client loggedInClient = (Client) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String loggedInClientId = loggedInClient.getId();

        // Check if the logged-in client is the same as the one being accessed/updated
        if (!loggedInClientId.equals(id) || !SecurityContextHolder.getContext().getAuthentication().getAuthorities().contains(new SimpleGrantedAuthority("CLIENT"))) {
            return "UNAUTHORIZED";
        }
        return "AUTHORIZED"; // Everything is okay
    }

}
