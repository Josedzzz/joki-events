package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    /**
     * Update an existing client by id
     * @param id String
     * @param dto UpdateClientDTO
     * @return ResponseEntity
     */
    @PostMapping("/{id}/update")
    public ResponseEntity<ApiTokenResponse<String>> updateClient(@PathVariable String id, @RequestBody UpdateClientDTO dto) {
        try {
            Map<Client, String> newPossibleLoginInfo = clientService.updateClient(id, dto);
            Map.Entry<Client, String> entry = newPossibleLoginInfo.entrySet().iterator().next();
            ApiTokenResponse<String> response = getResponse(entry); // IntelliJ generated method
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (NotAuthorizedException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        catch (UpdateClientException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        catch (AccountNotFoundException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    private static ApiTokenResponse<String> getResponse(Map.Entry<Client, String> entry) {
        Client client = entry.getKey();
        String token = entry.getValue();

        String optionalMessage = "";

        if (!client.isActive()) {
            optionalMessage += "\nEmail changed, please confirm new email in the email we sent\n Account deactivated until then";
        }

        return new ApiTokenResponse<>(
                "Success",
                "Client updated successfully" + optionalMessage,
                client.getId(),
                token
        );
    }

    /**
     * Delete client by id
     * @param clientId String
     * @return a ResponseEntity object with and HTTP status
     */
    @PostMapping("/{clientId}/delete")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@PathVariable String clientId) {
        try {
            clientService.deleteAccount(clientId);
            ApiResponse<String> response = new ApiResponse<>("Success", "Client account deleted", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NotAuthorizedException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } catch (AccountNotFoundException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete client", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("/{clientId}/verify")
    public ResponseEntity<ApiResponse<String>> verifyClient(@PathVariable String clientId, @RequestParam String verificationCode) {
        try {
            clientService.verifyClient(clientId, verificationCode);
            ApiResponse<String> response = new ApiResponse<>("Success", "Client has been verified and activated", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountNotFoundException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (ClientAlreadyActiveException e) {
            ApiResponse<String> response = new ApiResponse<>("Success", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (VerificationCodeExpiredException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.GONE);
        } catch (IncorrectVerificationCodeException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "An error occurred during verification", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/get-paginated-events")
    public ResponseEntity<?> getAllEventsPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        return clientService.getAllEventsPaginated(page, size);
    }

    @GetMapping("/get-client-account-info/{clientId}")
    public ResponseEntity<ApiResponse<?>> getAccountInformation(@PathVariable String clientId) {
        try {
            UpdateClientDTO clientInfo = clientService.getAccountInformation(clientId);
            ApiResponse<UpdateClientDTO> response = new ApiResponse<>("Success", "Client info retrieved", clientInfo);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (NotAuthorizedException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } catch (AccountNotFoundException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to retrieve client info", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    /**
     * Same input JSON as orderLocality method.
     * @param clientId String
     * @param dto LocalityOrderAsClientDTO
     * @return ResponseEntity
     */
    @PostMapping("/cancel-locality-order/{clientId}")
    public ResponseEntity<?> cancelLocalityOrder(@PathVariable String clientId, @RequestBody LocalityOrderAsClientDTO dto) {
        return clientService.cancelLocalityOrder(clientId, dto);
    }

    @GetMapping("/load-shopping-cart/{clientId}")
    public ResponseEntity<?> loadShoppingCart(@PathVariable String clientId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        return clientService.loadShoppingCart(clientId, page, size);
    }

    /**
     * Check if that String couponName must be changed to a dto.
     * @param clientId String
     * @param couponName String
     * @return ResponseEntity
     */
    @PostMapping("/apply-coupon/{clientId}")
    public ResponseEntity<?> applyCoupon(@PathVariable String clientId, String couponName) {
        return clientService.applyCoupon(clientId, couponName);
    }
}
