package com.uq.jokievents.controller;

import com.uq.jokievents.service.interfaces.ClientService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.uq.jokievents.dtos.LoginClientDTO;
import com.uq.jokievents.dtos.RegisterClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;

import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    @Autowired
    private ClientService clientService;
    /**
     * Get all clients
     *
     * @return a ResponseEntity object with all the contained clients
     */
    @GetMapping
    public ResponseEntity<?> getAllClients() {
        return clientService.findAllClients();
    }

    /**
     * Get a client by id
     *
     * @param id the identifier object of the client to find
     * @return a ResponseEntity containing the client
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable String id) {
        return clientService.findClientById(id);
    }

    /**
     * Update an existing client by id
     * Example JSON:
     * {
     *  "idCard": "1090900900",
     *  "phone": "3101112222",
     *  "email": "mail@mail.com",
     *  "name": "VeryCoolName",
     *  "direction": "Very Cool Addres"
     * }
     * @param id     the identifier of the client to update
     * @param client the updated client object
     * @return a ResponseEntity containing the update client
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable String id, @RequestBody UpdateClientDTO client) {
        return clientService.updateClient(id, client);
    }

    /**
     * Delete client by id
     *
     * @param id the identifier of the client to delete
     * @return a ResponseEntity object with and HTTP status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable String id) {
        return clientService.deleteClient(id);
    }

    /**
     * Login client with email and password
     * Example JSON:
     * {
     *  "email": "mail@mail.com",
     *  "password": "non-encrypted-password"
     * }
     * @param body the logindto
     * @return a ResponseEntity containing the client if found, otherwise an error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginClient(@RequestBody LoginClientDTO dto) {
        return clientService.findClientByEmailAndPassword(dto);
    }

    /**
     * Registers a client being aware of all its parameters
     * Example JSON:
     * {
     *  "idCard": "1090900900",
     *  "name": "VeryCoolName",
     *  "address": "Very cool address",
     *  "phone": "3003003000",
     *  "email": "mail@mail.com",
     *  "password": "Non-encrypted-password"
     * }
     * @param rcDto the dto that brings the front.
     * @return an entity response.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerClient(@Valid @RequestBody RegisterClientDTO rcDto) {
        // Using the service to register the client
        return clientService.registerNewClient(rcDto);
    }

    /**
     * Verify a clients code
     * Jose auth this!
     * Example JSON:
     * {
     *  "verificationCode": "123456"
     * }
     * @param body the dto that bring the front
     * @return an entity response
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyClient(@PathVariable String id, @RequestBody VerifyClientDTO dto) {
        return clientService.verifyCode(id, dto);
    }

    /**
     * Check if an email is already in use
     *
     * @param email the email to check
     * @return a ResponseEntity with the result of the check
     */
    @GetMapping("/existsByEmail")
    public ResponseEntity<?> existsByEmail(@RequestParam String email) {
        return clientService.existsByEmail(email);
    }

    /**
     * Check if an idCard is already in use
     *
     * @param idCard the email to check
     * @return a ResponseEntity with the result of the check
     */
    @GetMapping("/existsByIdCard")
    public ResponseEntity<?> existsIdCard(@RequestParam String idCard) {
        return clientService.existsByIdCard(idCard);
    }
}
