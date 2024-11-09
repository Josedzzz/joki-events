package com.uq.jokievents.controller;

import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.dtos.SearchEventDTO;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ApiTokenResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseEntity;

import com.uq.jokievents.dtos.UpdateClientDTO;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping("/{clientId}/verify")
    public ResponseEntity<ApiResponse<String>> verifyClient(@PathVariable String clientId, @RequestParam String verificationCode) {
        try {
            clientService.verifyClient(clientId, verificationCode);
            ApiResponse<String> response = new ApiResponse<>("Success", "Client has been verified and activated", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException | LogicException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/{clientId}/update")
    public ResponseEntity<ApiTokenResponse<String>> updateClient(@PathVariable String clientId, @Valid @RequestBody UpdateClientDTO dto) {
        try {
            Map<Client, String> newPossibleLoginInfo = clientService.updateClient(clientId, dto);
            Map.Entry<Client, String> entry = newPossibleLoginInfo.entrySet().iterator().next();
            ApiTokenResponse<String> response = getResponse(entry); // IntelliJ generated method
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (AuthorizationException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        catch (AccountException e) {
            ApiTokenResponse<String> response = new ApiTokenResponse<>("Error", e.getMessage(), null, null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
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
                "Client updated successfully " + optionalMessage,
                client.getId(),
                token
        );
    }

    @PostMapping("/{clientId}/delete-account")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@PathVariable String clientId) {
        try {
            clientService.deleteAccount(clientId);
            ApiResponse<String> response = new ApiResponse<>("Success", "Client account deleted", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/get-paginated-events")
    public ResponseEntity<ApiResponse<?>> getAllEventsPaginated(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        try {
            Map<String, Object> paginatedEvents = clientService.getAllEventsPaginated(page, size);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Success", "Retrieving the events", paginatedEvents);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/search-event")
    public ResponseEntity<?> searchEvent(@RequestBody SearchEventDTO dto, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "16") int size) {
        try {
            Map<String, Object> paginatedQueryEvents = clientService.searchEvent(dto, page, size);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Success", "Retrieving the events", paginatedQueryEvents);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/{clientId}/get-client-account-info")
    public ResponseEntity<ApiResponse<?>> getAccountInformation(@PathVariable String clientId) {
        try {
            UpdateClientDTO clientInfo = clientService.getAccountInformation(clientId);
            ApiResponse<UpdateClientDTO> response = new ApiResponse<>("Success", "Client info retrieved", clientInfo);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AuthorizationException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } catch (AccountException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{clientId}/order-locality")
    public ResponseEntity<?> orderLocality(@PathVariable String clientId, @RequestBody  LocalityOrderAsClientDTO dto) {
        try {
            clientService.orderLocality(clientId, dto);
            ApiResponse<String> response = new ApiResponse<>("Success", "Locality ordered successfully", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{clientId}/cancel-locality-order")
    public ResponseEntity<?> cancelLocalityOrder(@PathVariable String clientId, @RequestBody LocalityOrderAsClientDTO dto) {

        try {
            clientService.cancelLocalityOrder(clientId, dto);
            ApiResponse<String> response = new ApiResponse<>("Success", "Client order cancelled", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{clientId}/load-shopping-cart")
    public ResponseEntity<?> loadShoppingCart(@PathVariable String clientId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {
        try {
            Map<String, Object> clientShoppingCart = clientService.loadShoppingCart(clientId, page, size);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Success", "Client shopping cart loaded", clientShoppingCart);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * todo Check if that String couponName must be changed to a dto.
     * most likely not
     * @param clientId String
     * @param couponName String
     * @return ResponseEntity
     */
    @PostMapping("/{clientId}/apply-coupon")
    public ResponseEntity<ApiResponse<String>> applyCoupon(@PathVariable String clientId, @RequestParam String couponName) {
        try {
            clientService.applyCoupon(clientId, couponName);
            ApiResponse<String> response = new ApiResponse<>("Success", "Coupon applied successfully", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (AccountException | ShoppingCartException e ) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (LogicException | PaymentException e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "An unexpected error occurred", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{clientId}/purchase-history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPurchaseHistory(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ApiResponse<Map<String, Object>> response = clientService.loadPurchaseHistory(clientId, page, size);
        // Fuck Up Some Commas
        HttpStatus status = "Success".equals(response.getStatus()) ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(response, status);
    }

}
