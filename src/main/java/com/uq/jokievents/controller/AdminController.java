package com.uq.jokievents.controller;

import com.uq.jokievents.model.Admin;
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
    public ResponseEntity<List<Admin>> getAllAdmins() {
        List<Admin> admins = adminService.findAll();
        return new ResponseEntity<>(admins, HttpStatus.OK);
    }

    /**
     * Gets an admin by its id
     *
     * @param id the identifier
     * @return a ResponseEntity containing the admin object and an HTTP status of ok if found, otherwise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getAdminById(@PathVariable String id) {
        Optional<Admin> admin = adminService.findById(id);
        return admin.map(value -> new ResponseEntity<>(value, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Updates an existing admin
     *
     * @param id the identifier of the admin to be updated
     * @param admin a ResponseEntity containing the updated admin object and an HTTP status of ok, otherwise not found
     * @return
     */
    @PutMapping("/{id}")
    public ResponseEntity<Admin> updateAdmin(@PathVariable String id, @RequestBody Admin admin) {
        Optional<Admin> existingAdmin = adminService.findById(id);
        if (existingAdmin.isPresent()) {
            admin.setId(id);
            Admin updatedAdmin = adminService.save(admin);
            return new ResponseEntity<>(updatedAdmin, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes an Admin by its identifier
     *
     * @param id the identifier of the admin to be deleted
     * @return a ResponseEntity with an HTTP status of ok if the deletion is succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String id) {
        adminService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
