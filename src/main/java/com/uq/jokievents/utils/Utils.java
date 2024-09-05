package com.uq.jokievents.utils;

import com.uq.jokievents.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Utils {

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Verifies if a Email exists in the database
     *
     * @param clientEmail the Email of the client to check
     * @return true if the Email exists, false otherwise
     */
    public boolean existsEmailClient(String clientEmail) {
        return clientRepository.existsByEmail(clientEmail);
    }

    /**
     * Verifies if a IdCard exists in the database
     *
     * @param clientIdCard the idCard of the client to check
     * @return true if the Email exists, false otherwise
     */
    public boolean existsByIdCard(String clientIdCard) {
        return clientRepository.existsByIdCard(clientIdCard);
    }

}
