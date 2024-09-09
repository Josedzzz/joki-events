package com.uq.jokievents.service;

import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    /**
     * Get a list of all admins from the db
     *
     * @return a list of admins objects in the db
     */
    public ResponseEntity<?> findAll() {
        try {
            List<Admin> admins =  adminRepository.findAll();
            return new ResponseEntity<>(admins, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed admins request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets an admin by its id from the db
     *
     * @param id the identifier of the admin
     * @return an Optional containing the admin if found, empty Optional if not
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<Admin> admin = adminRepository.findById(id);
            if (admin.isPresent()) {
                return new ResponseEntity<>(admin.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed admins request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new admin
     *
     * @param admin the admin object to be created
     * @return a ResponseEntity containing the created admin object and an HTTP status
     */
    public ResponseEntity<?> create(Admin admin) {
        try {
            Admin createdAdmin = adminRepository.save(admin);
            return new ResponseEntity<>(createdAdmin, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Saves a new admin or updates an existing in the db
     *
     * @param admin the admin object to be saved or updated
     * @return the saved or updated admin object
     */
    public ResponseEntity<?> update(String id, Admin admin) {
        try {
            Optional<Admin> existingAdmin = adminRepository.findById(id);
            if (existingAdmin.isPresent()) {
                admin.setId(id);
                Admin updatedAdmin = adminRepository.save(admin);
                return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to admins client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes an admin from the db using its id
     *
     * @param id the identifier of the admin to be deletec
     */
    public ResponseEntity<?> deleteById(String id) {
        try {
            Optional<Admin> existingAdmin = adminRepository.findById(id);
            if (existingAdmin.isPresent()) {
                adminRepository.deleteById(id);
                return new ResponseEntity<>("Admin deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Admin not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete admin", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Find an admin by email and password
     *
     * @param username of the admin
     * @param password of the admin
     * @return a ResponseEntity containing a JSON with the admin's id if found, otherwise a JSON with an error message
     */
    public ResponseEntity<?> findByUsernameAndPassword(LoginClientDTO dto) {
        try {
            String username = dto.getEmail();
            String password = dto.getPassword();
            Optional<Admin> admin = adminRepository.findByUsernameAndPassword(username, password);
            if (admin.isPresent()) {
                Map<String, String> response = new HashMap<>();
                response.put("id", admin.get().getId());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Invalid username or password for admin");
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to find the admin");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
