package com.uq.jokievents.utils;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationService {

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Function to verify a client email sent code.
     * @param clientId
     * @param code
     * @return
     */
    public boolean verifyCode(String clientId, String code) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (client.getVerificationCode() == null || client.getVerificationCodeExpiration() == null) {
            return false;
        }

        boolean isValid = client.getVerificationCode().equals(code) &&
                LocalDateTime.now().isBefore(client.getVerificationCodeExpiration());

        if (isValid) {
            client.setVerificationCode(null);
            client.setVerificationCodeExpiration(null);
            client.setActive(true);
            clientRepository.save(client);
        }

        return isValid;
    }
}
