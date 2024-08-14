package com.uq.jokievents.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
     * @return a list of all Client objects in the db
     */
    public List<Client> findAll() {
        return clientRepository.findAll();
    }

    /**
     * Gets a client by its unique id from the db
     * 
     * @param id unique identifier of the client
     * @return an Optional containing the client if found, empty Optional if not
     */
    public Optional<Client> findById(String id) {
        return clientRepository.findById(id);
    }

    /**
     * Saves a new Client or updates an existing in the db
     * 
     * @param client the Client object to be saved or updated
     * @return the saved or updated client object
     */
    public Client save(Client client) {
        return clientRepository.save(client);
    }

    /**
     * Deletes a Client from the db using its id
     * 
     * @param id the unique identifier of the client to be deleted
     */
    public void deleteById(String id) {
        clientRepository.deleteById(id);
    }
}
