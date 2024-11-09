package com.uq.jokievents.repository;

import com.uq.jokievents.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {

    Optional<Admin> findById(String id); // Weird ass case, but, without it Admins are not recognized in this project, cause might be we do not have a proper CreateAdmin method, and we add it manually.
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByUsername(String username);
}
