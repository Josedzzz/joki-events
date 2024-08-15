package com.uq.jokievents.service;

import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.repository.ShoppingCartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    /**
     * Get a list of all ShoppingCarts from the db
     *
     * @return a list of all ShoppingCarts objects in the db
     */
    public List<ShoppingCart> findAll() {
        return shoppingCartRepository.findAll();
    }

    /**
     * Gets a ShoppingCart by its unique id from the db
     *
     * @param id unique identifier of the ShoppinCart object
     * @return an Optional containing the ShoppingCart if found, empy Optional if not
     */
    public Optional<ShoppingCart> findById(String id) {
        return shoppingCartRepository.findById(id);
    }

    /**
     * Saves a new ShoppingCart or updates an existing in the db
     *
     * @param shoppingCart object to be saved or updated
     * @return the saved or updated ShoppingCart object
     */
    public ShoppingCart save(ShoppingCart shoppingCart) {
        return shoppingCartRepository.save(shoppingCart);
    }

    /**
     * Deletes a ShoppingCart from the db
     *
     * @param id the unique identifier of the object
     */
    public void deleteById(String id) {
        shoppingCartRepository.deleteById(id);
    }
}
