package com.uq.jokievents.utils;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VerificationService {

    @Autowired
    private ClientRepository clientRepository;
    private final ClientService clientService;


    /**
     * Function to verify a client code sent from email.
     * @param clientId String
     * @param code String
     * @return
     */
    public boolean verifyCode(String clientId, String code) {
        Optional<Client> client = clientService.getClientFromClientId(clientId);
        if(client.isEmpty()) {
            throw new RuntimeException("Client not found");
        } else {
            Client clientInstance = client.get();

            if (clientInstance.getVerificationCode() == null || clientInstance.getVerificationCodeExpiration() == null) {
                return false;
            }

            boolean isValid = clientInstance.getVerificationCode().equals(code) &&
                    LocalDateTime.now().isBefore(clientInstance.getVerificationCodeExpiration());

            if (isValid) {
                clientInstance.setVerificationCode(null);
                clientInstance.setVerificationCodeExpiration(null);
                clientInstance.setActive(true);
                clientService.saveClientInDatabase(clientInstance);
            }
            return isValid;
        }

    }
}
