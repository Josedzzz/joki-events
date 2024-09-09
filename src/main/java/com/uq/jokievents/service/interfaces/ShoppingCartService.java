package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.ShoppingCart;
import org.springframework.http.ResponseEntity;

public interface ShoppingCartService {

    ResponseEntity<?> findAll();
    ResponseEntity<?> findById(String id);
    ResponseEntity<?> create(ShoppingCart shoppingCart);
    ResponseEntity<?> update(String id, ShoppingCart shoppingCart);
    ResponseEntity<?> deleteById(String id);

}
