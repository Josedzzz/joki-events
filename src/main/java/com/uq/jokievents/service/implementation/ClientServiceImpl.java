package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.LoadLocalityOrdersForClient;
import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.exceptions.*;
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

    @Value("${image.not.found}")
    private String notFoundString;

    /**
     * Updates a client from a dto.
     * @param clientId String
     * @param dto UpdateClientDTO
     * @return ResponseEntity
     */
    @Override
    public Map<Client, String> updateClient(String clientId, @RequestBody UpdateClientDTO dto) {

        String verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse))) {
            throw new NotAuthorizedException("Not authorized to enter this endpoint");
        }

        try {
            Optional<Client> existingClient = clientRepository.findById(clientId);

            if (existingClient.isEmpty()) {
                throw new AccountNotFoundException("Account not found");
            }

            Client client = existingClient.get();
            // todo take into account possible null values of the dto

            if (!client.getPhoneNumber().equals(dto.phone())){
                client.setPhoneNumber(dto.phone());
            }

            // Si no es el mismo notificar para que active el correo de nuevo.
            if (!client.getEmail().equals(dto.email())) {
                client.setEmail(dto.email());
                client.setActive(false);
                client.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15));
                emailService.sendVerificationMail(dto.email(), Generators.generateRndVerificationCode());
            }

            if (!client.getName().equals(dto.name())){
                client.setName(dto.name());
            }
            if (!client.getAddress().equals(dto.address())) {
                client.setAddress(dto.address());
            }

            clientRepository.save(client);
            // Update the token as they payload would change as well.
            UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
            String newToken = jwtService.getClientToken(clientDetails);

            Map<Client, String> newPossibleLoginInfo = new HashMap<>();
            newPossibleLoginInfo.put(client, newToken);
            return newPossibleLoginInfo;
        } catch (UpdateClientException e) {
            throw new UpdateClientException(e.getMessage());
        }
    }

    @Override
    public void deleteAccount(String clientId) {

        String verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new NotAuthorizedException("Not authorized to delete this account");
        }

        Optional<Client> existingClient = clientRepository.findById(clientId);
        if (existingClient.isEmpty()) {
            throw new AccountNotFoundException("Client not found");
        }

        Client client = existingClient.get();
        client.setActive(false);
        clientRepository.save(client);
    }


    // todo make the ClientController have the EventService instead and call the method of the service
    @Override
    public ResponseEntity<?> getAllEventsPaginated(int page, int size) {
        return eventService.getAllEventsPaginated(page, size);
    }

    @Override
    public UpdateClientDTO getAccountInformation(String clientId) {

        String verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new NotAuthorizedException("Not authorized to view this account information");
        }

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new AccountNotFoundException("Client info not found");
        }

        Client client = clientOpt.get();
        return mapToUpdateClientDTO(client); // Converts `Client` to `UpdateClientDTO`
    }

    // Method generated by IntelliJ
    private UpdateClientDTO mapToUpdateClientDTO(Client client) {
        return new UpdateClientDTO(
                client.getPhoneNumber(), client.getEmail(), client.getName(), client.getAddress()
        );
    }

    /**
     * Orders a locality
     * todo adapt it to the Exception handling code by myself, chatgpt might mess up
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

    /**
     * Cancels a locality
     * todo convert it myself
     * @param clientId String
     * @param dto LocalityOrderAsClient
     * @return String
     */
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

    /**
     * Loads the shopping cart when a Client click on it
     * todo convert this myself and complete it
     * @param clientId String
     * @param page int
     * @param size int
     * @return void
     */
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
                                    "NoID",
                                    "Event not found, it may have been cancelled",
                                    "",
                                    "",
                                    null,
                                    notFoundString,
                                    null);
                    loadLocalityOrdersForClientsArray.add(loadLocalityOrdersForClient);
                } else if (!LocalDateTime.now().plusDays(2).isBefore(eventOptional.get().getEventDate())) {
                    // If the reserving date is at least two days from the event occurring. Do not show it.
                    // TODO Cancel this order, delete this shopping cart from the database. Will wait until the database won't be touched that frequently
                    continue;
                } else {
                    LoadLocalityOrdersForClient loadLocalityOrdersForClient = getLoadLocalityOrdersForClient(localityOrder, eventOptional);
                    loadLocalityOrdersForClientsArray.add(loadLocalityOrdersForClient);
                }
            }

            // Paginate the result
            int totalElements = loadLocalityOrdersForClientsArray.size();  // Total number of events found
            int totalPages = (int) Math.ceil((double) totalElements / size);  // Calculate total number of pages
            int startIndex = page * size;  // Calculate the start index for the page, this will be usually just be zero.
            int endIndex = Math.min(startIndex + size, totalElements);  // Calculate the end index for the page

            // This is just comparing if there are enough elements to show on a certain page. Usually 0 >= loadLocalityOrdersForClientsArray.size() will be evaluated.
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
                event.getId(),
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
    public void verifyClient(String clientId, String verificationCode) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AccountNotFoundException("Client not found"));

        // Verify if client is already active
        if (client.isActive()) {
            throw new ClientAlreadyActiveException("Client is already active");
        }

        // Check if the activation code has expired
        if (client.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new VerificationCodeExpiredException("Activation code expired");
        }

        // Check if the provided verification code matches
        if (!client.getVerificationCode().equals(verificationCode)) {
            throw new IncorrectVerificationCodeException("Incorrect verification code");
        }

        // If verification code is correct, activate the client
        client.setActive(true);
        clientRepository.save(client);
    }
}
