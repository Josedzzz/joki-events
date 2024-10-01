package com.uq.jokievents.service.implementation;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.Event;
import com.uq.jokievents.model.LocalityOrder;
import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.ShoppingCartService;
import com.uq.jokievents.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;

    /**
     * Get a list of all ShoppingCarts from the db
     *
     * @return a list of all ShoppingCarts objects in the db
     */
    public ResponseEntity<?> findAll() {
        try {
            List<ShoppingCart> shoppingCarts = shoppingCartRepository.findAll();
            return new ResponseEntity<>(shoppingCarts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed ShoppingCarts request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets a ShoppingCart by its unique id from the db
     *
     * @param id unique identifier of the ShoppinCart object
     * @return an Optional containing the ShoppingCart if found, empy Optional if not
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<ShoppingCart> shoppingCart = shoppingCartRepository.findById(id);
            if (shoppingCart.isPresent()) {
                return new ResponseEntity<>(shoppingCart.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("ShoppingCart not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed ShoppingCart request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves a new ShoppingCart or updates an existing in the db
     *
     * @param shoppingCart object to be saved or updated
     * @return the saved or updated ShoppingCart object
     */
    public ResponseEntity<?> create(ShoppingCart shoppingCart) {
        try {
            ShoppingCart createdShoppingCart = shoppingCartRepository.save(shoppingCart);
            return new ResponseEntity<>(createdShoppingCart, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create ShoppingCart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifierof the client to be update
     * @param shoppingCart the shoppingCart client object
     * @return a ResponseEntity containing the updated shoppingCart objec and an HTTP status
     */
    public ResponseEntity<?> update(String id, ShoppingCart shoppingCart) {
        try {
            Optional<ShoppingCart> existingShoppingCart = shoppingCartRepository.findById(id);
            if (existingShoppingCart.isPresent()) {
                shoppingCart.setId(id);
                ShoppingCart updatedShoppingCart = shoppingCartRepository.save(shoppingCart);
                return new ResponseEntity<>(updatedShoppingCart, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("ShoppingCart not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update shoppingCart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a ShoppingCart from the db
     *
     * @param id the unique identifier of the object
     */
    public ResponseEntity<?> deleteById(String id) {
        try {
            Optional<ShoppingCart> existingShoppingCart = shoppingCartRepository.findById(id);
            if (existingShoppingCart.isPresent()) {
                shoppingCartRepository.deleteById(id);
                return new ResponseEntity<>("ShoppingCart deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("ShoppingCart not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete ShoppingCart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Optional<ShoppingCart> findShoppingCartById(String idShoppingCart) {
        return shoppingCartRepository.findById(idShoppingCart);
    }

    @Override
    public void saveShoppingCart(ShoppingCart shoppingCart) {
        shoppingCartRepository.save(shoppingCart);
    }
}

