package com.uq.jokievents.service.interfaces;

import com.uq.jokievents.model.ShoppingCart;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface ShoppingCartService {

    Optional<ShoppingCart> findShoppingCartById(String idShoppingCart);
    void saveShoppingCart(ShoppingCart shoppingCart);
}
