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
     * Gets a list of all clients
     * 
     * @return a ResponseEntity containing a list of Client objects and an HTTP
     *         status of ok
     */
    @GetMapping
    public ResponseEntity<List<Client>> getAllClients() {
        List<Client> clients = clientService.findAll();
        return new ResponseEntity<>(clients, HttpStatus.OK);
    }

    /**
     * Gets a Client by its unique id
     * 
     * @param id the unique identifier of the Client
     * @return a ResponseEntity containing the Client object and HTTP status of ok
     *         if found, otherwhise the status is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Client> getClientById(@PathVariable String id) {
        Optional<Client> client = clientService.findById(id);
        return client.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new Client
     * 
     * @param client the Client object to be created
     * @return a ResponseEntity containing the created Client object and an HTTP
     *         status of created
     */
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        Client newClient = clientService.save(client);
        return new ResponseEntity<>(newClient, HttpStatus.CREATED);
    }

    /**
     * Updates an existing Client
     * 
     * @param id     the unique indentifier of the Client to be updated
     * @param client the Client object containing the updated data
     * @return a ResponseEntity containing the updated Client object and HTTP status
     *         of ok, otherwise not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Client> updateClient(@PathVariable String id, @RequestBody Client client) {
        Optional<Client> existingClient = clientService.findById(id);
        if (existingClient.isPresent()) {
            client.setId(id);
            Client updateClient = clientService.save(client);
            return new ResponseEntity<>(updateClient, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes a Client by its unique id
     * 
     * @param id the unique identifier of the client to be deleted
     * @return a ResponseEntity with an HTTP status of  ok if the deletion is
     *         succesful
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable String id) {
        clientService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
