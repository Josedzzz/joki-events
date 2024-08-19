package com.uq.jokievents.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.uq.jokievents.model.Client;
import com.uq.jokievents.repository.ClientRepository;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    /**
     * Get a list of all clients from the db
     *
     * @return a ResponseEntity containing a list of Client objects and an HTTP status of ok
     */
    public ResponseEntity<?> findAll() {
        try {
            List<Client> clients = clientRepository.findAll();
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed clients request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get a client by id
     *
     * @param id the identifier of the client object
     * @return a ResponseEntity containing the client and an HTTP status
     */
    public ResponseEntity<?> findById(String id) {
        try {
            Optional<Client> client = clientRepository.findById(id);
            if (client.isPresent()) {
                return new ResponseEntity<>(client.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed client request", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Create a new client
     *
     * @param client the client object to be created
     * @return a ResponseEntity containing the created client object and an HTTP status
     */
    public ResponseEntity<?> create(Client client) {
        try {
            Client createdClient = clientRepository.save(client);
            return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update an existing client by id
     *
     * @param id the identifierof the client to be update
     * @param client the updated client object
     * @return a ResponseEntity containing the updated client objec and an HTTP status
     */
    public ResponseEntity<?> update(String id, Client client) {
        try {
            Optional<Client> existingClient = clientRepository.findById(id);
            if (existingClient.isPresent()) {
                client.setId(id);
                Client updatedClient = clientRepository.save(client);
                return new ResponseEntity<>(updatedClient, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Delete a client by its id
     *
     * @param id the identifier of the client to delete
     * @return a ResponseEntity with an HTTP status
     */
    public ResponseEntity<?> delete(String id) {
        try {
            Optional<Client> existingClient = clientRepository.findById(id);
            if (existingClient.isPresent()) {
                clientRepository.deleteById(id);
                return new ResponseEntity<>("Client deleted", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to delete client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Find a client by email and password
     *
     * @param email of the client
     * @param password of the client
     * @return a ResponseEntity containig the client if found, otherwise a 404 status
     */
    public ResponseEntity<?> findByEmailAndPassword(String email, String password) {
        try {
            Optional<Client> client = clientRepository.findByEmailAndPassword(email, password);
            if (client.isPresent()) {
                return new ResponseEntity<>(client.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid email or password", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to find client", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
