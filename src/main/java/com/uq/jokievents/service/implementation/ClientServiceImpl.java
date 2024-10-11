package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.LoadLocalityOrdersForClient;
import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.model.*;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.repository.CouponRepository;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.JwtService;
import com.uq.jokievents.service.interfaces.ShoppingCartService;
import com.uq.jokievents.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final Utils utils;
    private final JwtService jwtService;
    private final EventService eventService;
    private final ShoppingCartService shoppingCartService;
    private final CouponRepository couponRepository;
    private final EmailService emailService;
    private final ShoppingCartRepository shoppingCartRepository;

    @Value("${jwt.image.not.found}")
    private String notFoundString;

    /**
     * Updates a client from a dto.
     * TODO Ms
     * @param clientId String
     * @param dto UpdateClientDTO
     * @return ResponseEntity
     */
    @Override
    public ResponseEntity<?> updateClient(String clientId, @RequestBody UpdateClientDTO dto) {

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        StringBuilder optionalMessages = new StringBuilder();

        try {
            Optional<Client> existingClient = clientRepository.findById(clientId);
            if (existingClient.isPresent()) {
                Client client = existingClient.get();
                System.out.println(client);

                // Si existe déjelo igual
                if (dto.email() != null && utils.existsEmailClient(dto.email())) {
                    client.setEmail(dto.email());
                } else {
                    optionalMessages.append("Email changed, please confirm new email in the email we sent\n Account deactivated until then");
                    client.setEmail(dto.email());
                    client.setActive(false);
                    client.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15));
                    emailService.sendVerificationMail(dto.email(), Generators.generateRndVerificationCode());
                }

                if (dto.phone() != null) {
                    client.setPhoneNumber(dto.phone());
                }
                if (dto.name() != null) {
                    client.setName(dto.name());
                }
                if (dto.address() != null) {
                    client.setAddress(dto.address());
                }

                clientRepository.save(client);
                // Update the token as they payload would change as well.
                UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
                String newToken = jwtService.getClientToken(clientDetails);

                ApiTokenResponse<Object> response = new ApiTokenResponse<>("Success","Client update done", client, newToken);
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
    public ResponseEntity<?> getAllEventsPaginated(int page, int size) {
        return eventService.getAllEventsPaginated(page, size);
    }

    @Override
    public ResponseEntity<?> getAccountInformation(String clientId) {

        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
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
        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

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

            // Validate payment amount, if this ever outputs I will be very confused. As of 10/10/2024 I've seen this output several times (pain in the ass)
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
        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

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
                            && order.getNumTicketsSelected() >= dto.ticketsSelected()
                            && order.getTotalPaymentAmount() >=  (dto.totalPaymentAmount()))
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
            event.setTotalAvailablePlaces(event.getTotalAvailablePlaces() + dto.ticketsSelected());

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
    // Should only load possible to buy LocalityOrders and its done but waiting to implement it when cleansing the database.
    // If this method does not work it may be due of EventRepository findByLocalitiesName() method.
    // FUCK SRP
    @Override
    public ResponseEntity<?> loadShoppingCart(String clientId, int page, int size) {
        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

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

            ArrayList<LocalityOrder> localityOrders = shoppingCart.getLocalityOrders();
            List<LoadLocalityOrdersForClient> loadLocalityOrdersForClientsArray = new ArrayList<>();

            for (LocalityOrder localityOrder : localityOrders) {

                Optional<Event> eventOptional = eventService.findByEventById(localityOrder.getEventId());
                if (eventOptional.isEmpty()) {
                    LoadLocalityOrdersForClient loadLocalityOrdersForClient =
                            new LoadLocalityOrdersForClient(
                                    localityOrder.getPayingOrderId(),
                                    localityOrder.getNumTicketsSelected(),
                                    localityOrder.getLocalityName(),
                                    localityOrder.getTotalPaymentAmount(),
                                    "Event not found, it may have been cancelled",
                                    "",
                                    "",
                                    null,
                                    notFoundString,
                                    null);
                    loadLocalityOrdersForClientsArray.add(loadLocalityOrdersForClient);
                } else if (!LocalDateTime.now().plusDays(2).isBefore(eventOptional.get().getEventDate())) {
                    // If the reserving date is at least two days from the event occurring. Do not show it.
                    // TODO Cancel this order, delete this shopping cart from the database
                    continue;
                } else {
                    LoadLocalityOrdersForClient loadLocalityOrdersForClient = getLoadLocalityOrdersForClient(localityOrder, eventOptional);
                    loadLocalityOrdersForClientsArray.add(loadLocalityOrdersForClient);
                }
            }

            // Paginate the result
            int totalElements = loadLocalityOrdersForClientsArray.size();  // Total number of events found
            int totalPages = (int) Math.ceil((double) totalElements / size);  // Calculate total number of pages
            int startIndex = page * size;  // Calculate the start index for the page
            int endIndex = Math.min(startIndex + size, totalElements);  // Calculate the end index for the page

            if (startIndex >= totalElements) {
                return new ResponseEntity<>(new ApiResponse<>("Success", "No locality orders", List.of()), HttpStatus.OK);
            }

            // Get the paginated sublist
            List<LoadLocalityOrdersForClient> paginatedEvents = loadLocalityOrdersForClientsArray.subList(startIndex, endIndex);

            // Prepare pagination metadata
            Map<String, Object> paginationData = new HashMap<>();
            paginationData.put("totalPages", totalPages);
            paginationData.put("currentPage", page);
            paginationData.put("totalElements", totalElements);
            paginationData.put("content", paginatedEvents);  // The paginated events for the current page

            ApiResponse<Map<String, Object>> response = new ApiResponse<>("Success", "Shopping cart loaded", paginationData);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "Failed to load shopping cart", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private static LoadLocalityOrdersForClient getLoadLocalityOrdersForClient(LocalityOrder localityOrder, Optional<Event> eventOptional) {
        Event event = eventOptional.get();
        return new LoadLocalityOrdersForClient(
                localityOrder.getPayingOrderId(),
                localityOrder.getNumTicketsSelected(),
                localityOrder.getLocalityName(),
                localityOrder.getTotalPaymentAmount(),
                event.getName(),
                event.getAddress(),
                event.getCity(),
                event.getEventDate(),
                event.getEventImageUrl(),
                event.getEventType());
    }

    @Override
    public ResponseEntity<?> applyCoupon(String clientId, String couponName) {
        ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if (verificationResponse != null) {
            return verificationResponse;
        }

        // Obtain the client to get the ShoppingCart
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if (clientOptional.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse<>("Error", "Client not found", null), HttpStatus.NOT_FOUND);
        }
        Client client = clientOptional.get();

        // Obtain the ShoppingCart
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartService.findShoppingCartById(client.getIdShoppingCart());
        if (shoppingCartOptional.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse<>("Grave error", "Shopping cart not found", null), HttpStatus.NOT_FOUND);
        }
        ShoppingCart clientShoppingCart = shoppingCartOptional.get();

        //
        if (clientShoppingCart.isCouponClaimed()){
            return new ResponseEntity<>(new ApiResponse<>("Error", "Only one coupon per ", null), HttpStatus.NOT_FOUND);
        }

        // Obtain the Coupon from the database, if the coupon does not exist return error
        Optional<Coupon> couponOptional = couponRepository.findByName(couponName);
        if (couponOptional.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse<>("Error", "No coupon with this name found", null), HttpStatus.NOT_FOUND);
        }
        Coupon coupon = couponOptional.get();

        // If there are no localities in the ShoppingCart check if the coupon can be applied, else return error.
        if(!clientShoppingCart.getLocalityOrders().isEmpty()) {
            Double totalPriceOfLocalityOrders = clientShoppingCart.getTotalPrice();
            if (totalPriceOfLocalityOrders < coupon.getMinPurchaseAmount()) {
                return new ResponseEntity<>(new ApiResponse<>("Error", "Minimum pay amount to use this coupon is " + coupon.getMinPurchaseAmount(), null), HttpStatus.NOT_FOUND);
            }
            // After all that checks, a Coupon can be used. The discount percent is generally a natural number from 1 to 99.
            // Updating the price with discount. Hope that the operation never fails, I don't want to try catch that.

            Double totalPriceOfLocalityOrderWithDiscount = totalPriceOfLocalityOrders * (1 - ( coupon.getDiscountPercent()/100 ));
            clientShoppingCart.setTotalPriceWithDiscount(totalPriceOfLocalityOrderWithDiscount);
            clientShoppingCart.setCouponClaimed(true);
            clientShoppingCart.setAppliedDiscountPercent((1 - ( coupon.getDiscountPercent()/100 )));
            client.getListOfUsedCoupons().add(couponName);

            // Saving all those changes to the database.
            clientRepository.save(client);
            shoppingCartRepository.save(clientShoppingCart);

            return new ResponseEntity<>(new ApiResponse<>("Error", "Minimum pay amount to use this coupon is " + coupon.getMinPurchaseAmount(), null), HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(new ApiResponse<>("Error", "No ordered localities to apply a coupon", null), HttpStatus.NOT_FOUND);
        }

    }

    @Override
    public ResponseEntity<?> verifyClient(String clientId, String verificationCode) {
        try {
            // Fetch the client from the repository
            Optional<Client> clientOptional = clientRepository.findById(clientId);

            // Check if the client exists
            if (clientOptional.isEmpty()) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Client not found", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            Client client = clientOptional.get();

            // Verify if client is already active
            if (client.isActive()) {
                ApiResponse<String> response = new ApiResponse<>("Success", "Client is already active", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            if (!client.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
                ApiResponse<String> response = new ApiResponse<>("Error", "Activation code expired", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            }

            boolean correctCode = client.getVerificationCode().equals(verificationCode);

            // Check if the  code is the same as the one stored in the database
            if (correctCode) {
                // Set the client as active
                client.setActive(true);
            }

            clientRepository.save(client);

            // Send an email confirmation
            // TODO Implement this
            // emailService.sendVerificationSuccessEmail(client.getEmail());

            ApiResponse<String> response = new ApiResponse<>("Success", "Client has been verified and activated", null);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", "An error occurred during verification", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // Method generated by IntelliJ
    private static ApiResponse<UpdateClientDTO> getUpdateClientDTOApiResponse(Optional<Client> client) {
        String phone = client.get().getPhoneNumber();
        String email = client.get().getEmail();
        String name = client.get().getName();
        String address = client.get().getAddress();

        UpdateClientDTO dto = new UpdateClientDTO(phone, email, name, address);
        return new ApiResponse<>("Success", "Client info returned", dto);
    }
}
