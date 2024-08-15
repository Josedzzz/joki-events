package com.uq.jokievents.repository;

import com.uq.jokievents.model.ShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends MongoRepository<ShoppingCart, String> {
}
