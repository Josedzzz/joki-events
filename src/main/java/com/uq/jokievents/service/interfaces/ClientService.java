package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.model.Client;
import org.springframework.http.ResponseEntity;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;

import java.util.Optional;


public interface ClientService {
    ResponseEntity<?> updateClient(String clientId, UpdateClientDTO dto);
    ResponseEntity<?> deleteAccount(String id);

    ResponseEntity<?> getAllEventsPaginated(int page, int size);
    ResponseEntity<?> getAccountInformation(String clientId);
    ResponseEntity<?> orderLocality(String clientId, LocalityOrderAsClientDTO dto);
    ResponseEntity<?> cancelLocalityOrder(String clientId, LocalityOrderAsClientDTO dto);
    ResponseEntity<?> loadShoppingCart(String clientId);
    ResponseEntity<?> applyCoupon(String clientId, String coupon);
}
