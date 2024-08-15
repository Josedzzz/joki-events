package com.uq.jokievents.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.service.ClientService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    /**
     * Get all clients
     *
     * @return a ResponseEntity objest with containing clients
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
     * @return a ReponseEntity containing the update client
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

}
