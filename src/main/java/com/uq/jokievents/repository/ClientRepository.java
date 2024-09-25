package com.uq.jokievents.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.uq.jokievents.model.Client;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {
        Optional<Client> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByIdCard(String idCard);
}
