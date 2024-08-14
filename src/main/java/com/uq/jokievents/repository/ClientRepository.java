package com.uq.jokievents.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.uq.jokievents.model.Client;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {

}
