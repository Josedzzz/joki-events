package com.uq.jokievents.controller;

import com.uq.jokievents.records.LoginDTO;
import com.uq.jokievents.records.RegisterClientDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.service.ClientService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
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
        return clientService.findAll();
    }

    /**
     * Get a client by id
     *
     * @param id the identifier object of the client to find
     * @return a ResponseEntity containing the client
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getClientById(@PathVariable String id) {
        return clientService.findById(id);
    }

    /**
     * Create a new client
     *
     * @param client the client object to be created
     * @return a ResponseEntity containing the created Client
     */
    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody Client client) {
        return clientService.create(client);
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifier of the client to update
     * @param client the updated client object
     * @return a ResponseEntity containing the update client
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable String id, @RequestBody Client client) {
        return clientService.update(id, client);
    }

    /**
     * Delete client by id
     *
     * @param id the identifier of the client to delete
     * @return a ResponseEntity object with and HTTP status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable String id) {
        return clientService.delete(id);
    }

    /**
     * Login client with email and password
     * Why return the client dto as an answer
     * @param body the logindto
     * @return a ResponseEntity containing the client if found, otherwise an error message
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginClient(@RequestBody LoginDTO body) {
        return clientService.findByEmailAndPassword(body.email(), body.password());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerClient(@RequestBody RegisterClientDTO rcDto) {
        // Using the service to register the client
        Client newClient = clientService.registerNewClient(rcDto);
        // Returning the client id, for a customized page for the new client!
        Map<String, String> response = new HashMap<>();
        response.put("id", newClient.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
