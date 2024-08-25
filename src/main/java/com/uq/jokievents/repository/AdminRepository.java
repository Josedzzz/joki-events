package com.uq.jokievents.repository;

import com.uq.jokievents.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {
    Optional<Admin> findByUsernameAndPassword(String username, String password);
}
