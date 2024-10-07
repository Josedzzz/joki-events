package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.dtos.VerifyClientDTO;
import com.uq.jokievents.model.*;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.JwtService;
import com.uq.jokievents.service.interfaces.ShoppingCartService;
import com.uq.jokievents.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;

import javax.validation.Valid;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final Utils utils;
    private final JwtService jwtService;
    private final EventService eventService;
    private final ShoppingCartService shoppingCartService;

    /**
     * Updates a client from a dto.
     * @param clientId String
     * @param dto UpdateClientDTO
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<?> updateClient(String clientId, @RequestBody UpdateClientDTO dto) {

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            Optional<Client> existingClient = clientRepository.findById(clientId);
            if (existingClient.isPresent()) {
                Client client = existingClient.get();

                // Verifications for Client update
                if (!client.getIdCard().equals(dto.idCard()) && utils.existsByIdCard(dto.idCard())) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "The identification card is in use", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }
                if (!client.getEmail().equals(dto.email()) && utils.existsEmailClient(dto.email())) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "The email is in use", null);
                    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                }

                // Update client details
                client.setIdCard(dto.idCard());
                client.setPhoneNumber(dto.phone());
                client.setEmail(dto.email());
                client.setName(dto.name());
                client.setAddress(dto.address());

                Client updatedClient = clientRepository.save(client);
                // Actualizo el token también
                UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
                String newToken = jwtService.getAdminToken(clientDetails);

                ApiTokenResponse<Object> response = new ApiTokenResponse<>("Success","Client update done", updatedClient, newToken);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Client not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to update client", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<?> deleteAccount(String clientId) {

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if (verificationResponse != null) {
            return verificationResponse;
        }

        try {
            Optional<Client> existingClient = clientRepository.findById(clientId);
            if (existingClient.isPresent()) {
                Client client = existingClient.get();
                client.setActive(false);
                clientRepository.save(client);
                ApiResponse<String> response = new ApiResponse<>("Success", "Client account deleted", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Client not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to delete client", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> verifyCode(String clientId, @Valid VerifyClientDTO dto) {

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Client not found", null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Client client = clientOpt.get();
        if (client.getVerificationCode() == null || client.getVerificationCodeExpiration() == null) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Verification code expired", null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        boolean isVerified = client.getVerificationCode().equals(dto.verificationCode()) &&
                LocalDateTime.now().isBefore(client.getVerificationCodeExpiration());

        if (isVerified) {
            client.setVerificationCode(null);
            client.setVerificationCodeExpiration(null);
            client.setActive(true);
            clientRepository.save(client);

            return ResponseEntity.ok(new ApiResponse<>("Success", "Client verification done", null));
        }

        return ResponseEntity.badRequest().body(new ApiResponse<>("Error", "Invalid code or time expired", null));
    }


    @Override
    public ResponseEntity<?> existsByEmail(String email) {
        try {
            boolean exists = clientRepository.existsByEmail(email);
            if (exists) {
                ApiResponse<String> response = new ApiResponse<>("Error", "The email is in use", null);
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Success", "The email is available", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to check existence of email", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> existsByIdCard(String idCard) {
        try {
            boolean exists = clientRepository.existsByIdCard(idCard);
            if (exists) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Identification card is already in use", null);
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Success", "Identification card is available", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to check identification card", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> getAllEventsPaginated(int page, int size) {
        return eventService.getAllEventsPaginated(page, size);
    }

    @Override
    public ResponseEntity<?> getAccountInformation(String clientId) {
        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if (verificationResponse != null) {
            return verificationResponse;
        }
        try {
            Optional<Client> client = clientRepository.findById(clientId);
            if (client.isPresent()) {
                ApiResponse<UpdateClientDTO> response = getUpdateClientDTOApiResponse(client);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Client info not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to retrieve client info", null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Step 1: Find the event by ID
     * Step 2: Find the locality in the event
     * Step 3: Update the locality's capacity
     * Step 4: Save the updated event with the new locality capacity
     * Step 5: Find the client by their ID
     * Step 6: Find the client's shopping cart
     * Step 7: Create the LocalityOrder and add it to the ShoppingCart
     * Step 8: Update the total price in the shopping cart
     * Step 9: Save the updated shopping cart
     * Step 10: Success
     * @param clientId String
     * @param dto LocalityOrderAsClientDTO
     * @return ResponseEntity<?>
     */
    @Override
    public ResponseEntity<?> orderLocality(String clientId, LocalityOrderAsClientDTO dto) {
        try {
            Optional<Event> eventOptional = eventService.getEventById(dto.eventId());

            if (eventOptional.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Event not found for the specified locality", null), HttpStatus.NOT_FOUND);
            }

            Event event = eventOptional.get();

            // Ensure the event is available for purchase
            if (!event.isAvailableForPurchase()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Event is not available for purchase", null), HttpStatus.BAD_REQUEST);
            }

            // Find the specified locality within the event, every locality must have a different name
            Optional<Locality> localityOpt = event.getLocalities().stream()
                    .filter(locality -> locality.getName().equals(dto.localityName()))
                    .findFirst();

            if (localityOpt.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Locality not found in the event", null), HttpStatus.NOT_FOUND);
            }

            Locality locality = localityOpt.get();

            // Validate ticket availability and reduce available tickets of locality
            if (dto.ticketsSelected() > locality.getMaxCapacity()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Not enough tickets available", null), HttpStatus.BAD_REQUEST);
            }
            locality.setMaxCapacity(locality.getMaxCapacity() - dto.ticketsSelected());

            // Validate payment amount, if this ever outputs I will be very confused
            double expectedPayment = dto.ticketsSelected() * locality.getPrice();
            if (dto.totalPaymentAmount() < expectedPayment) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Incorrect payment amount", null), HttpStatus.BAD_REQUEST);
            }

            // Save the event back to the database
            eventService.saveEvent(event);

            // Now the client logic, first find the client
            Optional<Client> optionalClient = clientRepository.findById(clientId);
            if (optionalClient.isEmpty()) {
                return new ResponseEntity<>("Client not found", HttpStatus.NOT_FOUND);
            }
            Client client = optionalClient.get();

            // Find the ShoppingCart
            Optional<ShoppingCart> optionalShoppingCart = shoppingCartService.findShoppingCartById(client.getIdShoppingCart());
            if (optionalShoppingCart.isEmpty()) {
                return new ResponseEntity<>("Shopping cart not found", HttpStatus.NOT_FOUND);
            }
            ShoppingCart shoppingCart = optionalShoppingCart.get();

            // Create the LocalityOrder and add it to the ShoppingCart
            LocalityOrder localityOrder = new LocalityOrder();
            localityOrder.setEventId(dto.eventId());
            localityOrder.setLocalityName(dto.localityName());
            localityOrder.setNumTicketsSelected(dto.ticketsSelected());
            localityOrder.setTotalPaymentAmount(dto.totalPaymentAmount());

            shoppingCart.getLocalityOrders().add(localityOrder);

            double updatedTotalPrice = shoppingCart.getLocalityOrders().stream()
                    .mapToDouble(LocalityOrder::getTotalPaymentAmount)
                    .sum();
            shoppingCart.setTotalPrice(updatedTotalPrice);

            shoppingCartService.saveShoppingCart(shoppingCart);

            ApiResponse<String> response = new ApiResponse<>("Success", "Locality ordered successfully", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to process order", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<?> cancelLocalityOrder(String clientId, LocalityOrderAsClientDTO dto) {
        try {
            // Find the client
            Optional<Client> clientOptional = clientRepository.findById(clientId);
            if (clientOptional.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Client not found", null), HttpStatus.NOT_FOUND);
            }

            Client client = clientOptional.get();

            // Find the client's shopping cart
            Optional<ShoppingCart> shoppingCartOptional = shoppingCartService.findShoppingCartById(client.getIdShoppingCart());
            if (shoppingCartOptional.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Shopping cart not found", null), HttpStatus.NOT_FOUND);
            }

            ShoppingCart shoppingCart = shoppingCartOptional.get();

            // Find the LocalityOrder to be canceled
            LocalityOrder orderToCancel = shoppingCart.getLocalityOrders().stream()
                    .filter(order -> order.getLocalityName().equals(dto.localityName())
                            && order.getNumTicketsSelected() == dto.ticketsSelected()
                            && order.getTotalPaymentAmount().equals(dto.totalPaymentAmount()))
                    .findFirst()
                    .orElse(null);

            if (orderToCancel == null) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Locality order not found in shopping cart", null), HttpStatus.NOT_FOUND);
            }

            // Update the Event (Restore tickets in the locality)
            Optional<Event> eventOptional = eventService.findByEventById(dto.eventId());
            if (eventOptional.isEmpty()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Event not found", null), HttpStatus.NOT_FOUND);
            }

            Event event = eventOptional.get();
            Locality localityToUpdate = event.getLocalities().stream()
                    .filter(locality -> locality.getName().equals(dto.localityName()))
                    .findFirst()
                    .orElse(null);

            if (localityToUpdate == null) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Locality not found in event", null), HttpStatus.NOT_FOUND);
            }

            // Restore the tickets in the locality
            localityToUpdate.setMaxCapacity(localityToUpdate.getMaxCapacity() + dto.ticketsSelected());

            // Remove the LocalityOrder from the shopping cart
            shoppingCart.getLocalityOrders().remove(orderToCancel);

            // Recalculate total price
            shoppingCart.setTotalPrice(shoppingCart.getLocalityOrders().stream()
                    .mapToDouble(LocalityOrder::getTotalPaymentAmount).sum());

            // Save the updated shopping cart and event
            shoppingCartService.saveShoppingCart(shoppingCart);
            eventService.saveEvent(event);

            // END
            ApiResponse<String> response = new ApiResponse<>("Success", "Locality order canceled and tickets restored", null);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to cancel locality order", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // This method should be called when a client clicks on the Shopping Cart button on the frontend.
    // Should only load possible to buy LocalityOrders
    // If this method does not work it may be due of EventRepository findByLocalitiesName() method.
    // FUCK SRP
    @Override
    public ResponseEntity<?> loadShoppingCart(String clientId) {
        try {

            // Find the client
            Optional<Client> clientOptional = clientRepository.findById(clientId);
            if (clientOptional.isEmpty()) {
                return null;
            }

            Client client = clientOptional.get();

            // Find the client's shopping cart
            Optional<ShoppingCart> shoppingCartOptional = shoppingCartService.findShoppingCartById(String.valueOf(client.getIdShoppingCart()));
            if (shoppingCartOptional.isEmpty()) {
                return null;
            }

            ShoppingCart shoppingCart = shoppingCartOptional.get();

            // Filter only the LocalityOrders that can still be purchased (event.availableForPurchase == true)
            List<LocalityOrder> validLocalityOrders = shoppingCart.getLocalityOrders().stream()
                    .filter(localityOrder -> {
                        Optional<Event> eventOptional = eventService.findEventByLocalityName(localityOrder.getLocalityName());
                        return eventOptional.isPresent() && eventOptional.get().isAvailableForPurchase();
                    })
                    .toList();

            // Update the shopping cart with the valid locality orders
            shoppingCart.setLocalityOrders(new ArrayList<>(validLocalityOrders));

            ApiResponse<ShoppingCart> response = new ApiResponse<>("Success", "Shopping cart loaded", shoppingCart);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to load shopping cart", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public Optional<Client> findClientById(String clientId) {
        return clientRepository.findById(clientId);
    }

    // Method generated by IntelliJ
    private static ApiResponse<UpdateClientDTO> getUpdateClientDTOApiResponse(Optional<Client> client) {
        String idCard = client.get().getIdCard();
        String phone = client.get().getPhoneNumber();
        String email = client.get().getEmail();
        String name = client.get().getName();
        String address = client.get().getAddress();

        UpdateClientDTO dto = new UpdateClientDTO(idCard, phone, email, name, address);
        return new ApiResponse<>("Success", "Client info returned", dto);
    }
}
