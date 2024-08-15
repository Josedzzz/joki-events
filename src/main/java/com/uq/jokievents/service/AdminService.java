package com.uq.jokievents.service;

import com.uq.jokievents.model.Admin;
import com.uq.jokievents.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public List<Admin> findAll() {
        return adminRepository.findAll();
    }

    /**
     * Gets an admin by its id from the db
     *
     * @param id the identifier of the admin
     * @return an Optional containing the admin if found, empty Optional if not
     */
    public Optional<Admin> findById(String id) {
        return adminRepository.findById(id);
    }

    /**
     * Saves a new admin or updates an existing in the db
     *
     * @param admin the admin object to be saved or updated
     * @return the saved or updated admin object
     */
    public Admin save(Admin admin) {
        return adminRepository.save(admin);
    }

    /**
     * Deletes an admin from the db using its id
     *
     * @param id the identifier of the admin to be deletec
     */
    public void deleteById(String id) {
        adminRepository.deleteById(id);
    }

}
