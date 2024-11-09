package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.dtos.SearchEventDTO;
import com.uq.jokievents.model.Client;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;

import java.util.Map;
import java.util.Optional;


public interface ClientService {
    Map<Client, String> updateClient(String clientId, UpdateClientDTO dto);
    void deleteAccount(String id);
    ResponseEntity<?> getAllEventsPaginated(int page, int size);
    ResponseEntity<?> searchEvent(SearchEventDTO dto, int page, int size);
    UpdateClientDTO getAccountInformation(String clientId);
    void orderLocality(String clientId, LocalityOrderAsClientDTO dto);
    void cancelLocalityOrder(String clientId, LocalityOrderAsClientDTO dto);
    Map<String, Object> loadShoppingCart(String clientId, int page, int size);
    void applyCoupon(String clientId, String coupon);

    void verifyClient(String clientId, String verificationCode);
}
