package com.uq.jokievents.controller;

import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shoppingcarts")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * Gets a list of all shoppingcarts
     *
     * @return a ResponseEntitu containing a lis of shoppingcarts objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<?> getAllShoppingCarts() {
        return shoppingCartService.findAll();
    }

    /**
     * Gets a shoppingcart bi its unique id
     *
     * @param id the unique identifier of the shoppingcart
     * @return a ResponseEntity containing the shoppingcart object and HTTP status of ok if found, otherwhise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getShoppingCartById(@PathVariable String id) {
        return shoppingCartService.findById(id);
    }

    /**
     * Creates a new shoppingcart
     *
     * @param shoppingCart the shoppingcart object to be created
     * @return a ResponseEntity containing the created shoppingcart object and an HTTP status of created
     */
    @PostMapping
    public ResponseEntity<?> createShoppingCart(@RequestBody ShoppingCart shoppingCart) {
        return shoppingCartService.create(shoppingCart);
    }

    /**
     * Updates an existing shoppingcart
     *
     * @param id the unique identifier of the shoppingcart object to be updated
     * @param shoppingCart the shoppingcart object containig the updated data
     * @return a ResponseEntity containing the update shoppingcart object and an HTTP status of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShoppingCart(@PathVariable String id, @RequestBody ShoppingCart shoppingCart) {
        return shoppingCartService.update(id, shoppingCart);
    }

    /**
     * Deletes a shoppingcart by its unique id
     *
     * @param id the unique identifier of the shoppingcart object to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping
    public ResponseEntity<?> deleteShoppingCart(@PathVariable String id) {
        return shoppingCartService.deleteById(id);
    }
}
