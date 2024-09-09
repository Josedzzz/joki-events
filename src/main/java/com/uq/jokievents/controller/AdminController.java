package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Gets a list of all admins
     *
     * @return a ResponseEntity containing a list of admin objects and an HTTP status of ok
     */
    @GetMapping
    public ResponseEntity<?> getAllAdmins() {
        return adminService.findAll();
    }

    /**
     * Gets an admin by its id
     *
     * @param id the identifier
     * @return a ResponseEntity containing the admin object and an HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(@PathVariable String id) {
        return adminService.findById(id);
    }

    /**
     * Create a new admin
     *
     * @param admin the admin object to be created
     * @return a ResponseEntity containing the created Admin
     */
    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody Admin admin) {
        return adminService.create(admin);
    }

    /**
     * Updates an existing admin
     *
     * @param id the identifier of the admin to be updated
     * @param admin a ResponseEntity containing the updated admin object and an HTTP status of ok, otherwise not found
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable String id, @RequestBody Admin admin) {
        return adminService.update(id, admin);
    }


    /**
     * Deletes an Admin by its identifier
     *
     * @param id the identifier of the admin to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable String id) {
        return adminService.deleteById(id);
    }

    /**
     * Login admin with email and password
     *
     * @param body the body with the email and the password
     * @return a ResponseEntity containing the admin's id if found, otherwise an error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginClientDTO dto) {
        return adminService.findByUsernameAndPassword(dto);
    }
}
