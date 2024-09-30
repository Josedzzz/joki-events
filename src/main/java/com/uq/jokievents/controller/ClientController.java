package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.service.interfaces.AuthenticationService;
import com.uq.jokievents.service.interfaces.ClientService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Update an existing client by id
     * Example JSON:
     * {
     *  "idCard": "1090900900",
     *  "phone": "3101112222",
     *  "email": "mail@mail.com",
     *  "localityName": "VeryCoolName",
     *  "direction": "Very Cool Address"
     * }
     * @param id     the identifier of the client to update
     * @param client the updated client object
     * @return a ResponseEntity containing the update client
     */
    @PostMapping("/{id}/update")
    public ResponseEntity<?> updateClient(@PathVariable String id, @RequestBody UpdateClientDTO client) {
        return clientService.updateClient(id, client);
    }

    /**
     * Delete client by id
     *
     * @param id the identifier of the client to delete
     * @return a ResponseEntity object with and HTTP status
     */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteAccount(@PathVariable String id) {
        return clientService.deleteAccount(id);
    }

    /**
     * Verify a clients code
     * Jose auth this!
     * Example JSON:
     * {
     *  "verificationCode": "123456"
     * }
     * @param dto the dto that bring the front
     * @return an entity response
     */
    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyClient(@PathVariable String id, @RequestBody VerifyClientDTO dto) {
        return clientService.verifyCode(id, dto);
    }

    /**
     * Check if an email is already in use
     *     * @param email the email to check
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

    @GetMapping("/get-paginated-events")
    public ResponseEntity<?> getAllEventsPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        return clientService.getAllEventsPaginated(page, size);
    }

    @GetMapping("/get-client-account-info/{clientId}")
    public ResponseEntity<?> getAccountInformation(@PathVariable String clientId) {
        return clientService.getAccountInformation(clientId);
    }

    /**
     * Example input JSON
     * {
     *     "eventId": "mongo-generated-id",
     *     "localityName": "VIP Section",
     *     "totalPaymentAmount": 150.00,
     *     "ticketsSelected": 3
     * }
     * @param dto LocalityOrderAsClientDTO
     * @return ResponseEntity
     */
    @PostMapping("/order-locality/{clientId}")
    public ResponseEntity<?> orderLocality(@PathVariable String clientId, @RequestBody LocalityOrderAsClientDTO dto) {
        return clientService.orderLocality(clientId, dto);
    }
}
