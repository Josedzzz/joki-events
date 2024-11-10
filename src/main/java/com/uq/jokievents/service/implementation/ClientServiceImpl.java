package com.uq.jokievents.service.implementation;

import com.uq.jokievents.dtos.LoadLocalityOrdersForClient;
import com.uq.jokievents.dtos.LocalityOrderAsClientDTO;
import com.uq.jokievents.dtos.SearchEventDTO;
import com.uq.jokievents.dtos.UpdateClientDTO;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.*;
import com.uq.jokievents.model.enums.EventType;
import com.uq.jokievents.repository.*;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.service.interfaces.JwtService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ClientSecurityUtils;
import com.uq.jokievents.utils.EmailService;
import com.uq.jokievents.utils.Generators;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final CouponRepository couponRepository;
    private final ShoppingCartRepository shoppingCartRepository;
    private final PurchaseRepository purchaseRepository;
    private final EventRepository eventRepository;
    private final EmailService emailService;
    private final JwtService jwtService;


    @Override
    public void verifyClient(String clientId, String verificationCode) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AccountException("Client not found"));

        // Verify if client is already active
        if (client.isActive()) {
            throw new AccountException("Client is already active");
        }

        // Check if the activation code has expired
        if (client.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new LogicException("Activation code expired");
        }

        // Check if the provided verification code matches
        if (!client.getVerificationCode().equals(verificationCode)) {
            throw new LogicException("Incorrect verification code");
        }

        // If verification code is correct, activate the client and reset the other necessary attributes
        client.setActive(true);
        client.setVerificationCode("");
        client.setVerificationCodeExpiration(LocalDateTime.now());
        clientRepository.save(client);
    }

    @Override
    public Map<Client, String> updateClient(String clientId, @RequestBody UpdateClientDTO dto) {

        try {
            Optional<Client> existingClient = clientRepository.findById(clientId);

            if (existingClient.isEmpty()) {
                throw new AccountException("Account not found");
            }

            Client client = existingClient.get();
            // Update or not everything, I can tell for sure it is a good petition of updating so why verify more?
            client.setPhoneNumber(dto.phone());
            // Si no es el mismo notificar para que active el correo de nuevo.
            if (!client.getEmail().equals(dto.email())) {
                newEmailVerification(dto, client);
            }
            client.setName(dto.name());
            client.setAddress(dto.address());

            clientRepository.save(client);
            // Update the token as they payload would change as well.
            UserDetails clientDetails = clientRepository.findById(clientId).orElse(null);
            String newToken = jwtService.getClientToken(clientDetails);

            Map<Client, String> newPossibleLoginInfo = new HashMap<>();
            newPossibleLoginInfo.put(client, newToken);
            return newPossibleLoginInfo;
        } catch (AccountException ignored) {
            throw new AccountException("Account not found");
        }
    }

    private void newEmailVerification(UpdateClientDTO dto, Client client) {
        client.setEmail(dto.email());
        client.setActive(false);
        String verificationCode = Generators.generateRndVerificationCode();
        client.setVerificationCode(verificationCode);
        client.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(15));
        emailService.sendVerificationMail(dto.email(), verificationCode);
    }

    @Override
    public void deleteAccount(String clientId) {

        Optional<Client> existingClient = clientRepository.findById(clientId);
        if (existingClient.isEmpty()) {
            throw new AccountException("Client not found");
        }

        Client client = existingClient.get();
        client.setActive(false);
        clientRepository.save(client);
    }

    @Override
    public Map<String, Object> getAllEventsPaginated(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Event> eventPage = eventRepository.findAll(pageable);

        // Populate pagination data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", eventPage.getContent());
        responseData.put("totalPages", eventPage.getTotalPages());
        responseData.put("totalElements", eventPage.getTotalElements());
        responseData.put("currentPage", eventPage.getNumber());

        return responseData;
    }

    @Override
    public Map<String, Object> searchEvent(SearchEventDTO dto, int page, int size) {

        String eventName = dto.eventName();
        String city = dto.city();
        LocalDateTime startDate = dto.startDate();
        LocalDateTime endDate = dto.endDate();
        EventType eventType = dto.eventType();

        // Fetch all events from the repository
        List<Event> allEvents = eventRepository.findAll();

        // Filter events based on the criteria
        // todo filter out inactive events, will do it when I figure out how the fuck Asynch methods work
        List<Event> filteredEvents = allEvents.stream()
                .filter(event ->
                        (eventName.isEmpty() || (event.getName() != null && event.getName().toLowerCase().contains(eventName.toLowerCase())))
                                && (city.isEmpty() || (event.getCity() != null && event.getCity().toLowerCase().contains(city.toLowerCase())))
                                && (startDate == null || (event.getEventDate() != null && !event.getEventDate().isBefore(startDate)))
                                && (endDate == null || (event.getEventDate() != null && !event.getEventDate().isAfter(endDate)))
                                && (eventType == null || eventType.equals(event.getEventType()))
                )
                .toList();

        // Calculate pagination
        int totalElements = filteredEvents.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        // Ensure the requested page is within bounds
        List<Event> paginatedEvents = (start >= totalElements) ? List.of() : filteredEvents.subList(start, end);

        // Prepare pagination metadata
        Map<String, Object> paginationData = new HashMap<>();
        paginationData.put("totalPages", totalPages);
        paginationData.put("currentPage", page);
        paginationData.put("totalElements", totalElements);
        paginationData.put("content", paginatedEvents);

        return paginationData;
    }

    @Override
    public UpdateClientDTO getAccountInformation(String clientId) {

        Optional<Client> clientOpt = clientRepository.findById(clientId);
        if (clientOpt.isEmpty()) {
            throw new AccountException("Client info not found");
        }

        Client client = clientOpt.get();
        return mapToUpdateClientDTO(client); // Converts `Client` to `UpdateClientDTO`
    }

    private UpdateClientDTO mapToUpdateClientDTO(Client client) {
        return new UpdateClientDTO(
                client.getPhoneNumber(), client.getEmail(), client.getName(), client.getAddress()
        );
    }

    @Override
    public void orderLocality(String clientId, LocalityOrderAsClientDTO dto) {

        // Retrieve the event
        Event event = eventRepository.findById(dto.eventId())
                .orElseThrow(() -> new EventException("Event not found for the specified locality"));

        // Check if the event is available for purchase
        if (!event.isAvailableForPurchase()) {
            throw new EventException("Event is not available for purchase");
        }

        // Find the specified locality within the event
        Locality locality = event.getLocalities().stream()
                .filter(loc -> loc.getName().equals(dto.localityName()))
                .findFirst()
                .orElseThrow(() -> new EventException("Locality not found in the event"));

        // Validate payment amount
        double expectedPayment = dto.selectedTickets() * locality.getPrice();
        if (dto.totalPaymentAmount() != expectedPayment) {
            throw new PaymentException("Incorrect payment amount");
        }

        // Save the event after ordering
        eventRepository.save(event);

        // Fetch the client
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AccountException("Client not found"));

        // Fetch the shopping cart
        ShoppingCart shoppingCart = shoppingCartRepository.findById(client.getIdShoppingCart())
                .orElseThrow(() -> new ShoppingCartException("Shopping cart not found"));

        // Create and add the locality order to the shopping cart
        LocalityOrder localityOrder = new LocalityOrder();
        localityOrder.setEventId(dto.eventId());
        localityOrder.setLocalityName(dto.localityName());
        localityOrder.setNumTicketsSelected(dto.selectedTickets());
        localityOrder.setTotalPaymentAmount(dto.totalPaymentAmount());

        shoppingCart.getLocalityOrders().add(localityOrder);

        // Update and save the shopping cart total price
        double newTotalPrice = shoppingCart.getTotalPrice() + expectedPayment;
        shoppingCart.setTotalPrice(newTotalPrice);
        shoppingCart.setTotalPriceWithDiscount(newTotalPrice);
        shoppingCartRepository.save(shoppingCart);
    }

    @Override
    public void cancelLocalityOrder(String clientId, LocalityOrderAsClientDTO dto) {
        // Find the client
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if (clientOptional.isEmpty()) {
            throw new AccountException("Client not found");
        }

        Client client = clientOptional.get();

        // Find the client's shopping cart
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findById(client.getIdShoppingCart());
        if (shoppingCartOptional.isEmpty()) {
            throw new ShoppingCartException("Shopping cart not found");
        }

        ShoppingCart shoppingCart = shoppingCartOptional.get();

        // Find the LocalityOrder to be canceled
        LocalityOrder orderToCancel = shoppingCart.getLocalityOrders().stream()
                .filter(order -> order.getLocalityName().equals(dto.localityName())
                        && order.getNumTicketsSelected() >= dto.selectedTickets()
                        && order.getTotalPaymentAmount() >= dto.totalPaymentAmount())
                .findFirst()
                .orElse(null);

        if (orderToCancel == null) {
            throw new EventException("Locality order not found in shopping cart");
        }

        // Update the Event (Restore tickets in the locality)
        Optional<Event> eventOptional = eventRepository.findById(dto.eventId());
        if (eventOptional.isEmpty()) {
            throw new EventException("Event not found");
        }

        Event event = eventOptional.get();
        Locality localityToUpdate = event.getLocalities().stream()
                .filter(locality -> locality.getName().equals(dto.localityName()))
                .findFirst()
                .orElse(null);

        if (localityToUpdate == null) {
            throw new EventException("Locality not found in event");
        }

        // Remove the LocalityOrder from the shopping cart
        shoppingCart.getLocalityOrders().remove(orderToCancel);

        // Recalculate total price
        double priceToRestar = localityToUpdate.getPrice() * dto.selectedTickets();
        shoppingCart.setTotalPrice(shoppingCart.getTotalPrice() - priceToRestar);

        // Save the updated shopping cart and event
        shoppingCartRepository.save(shoppingCart);
        eventRepository.save(event);
    }

    @Override
    public Map<String, Object> loadShoppingCart(String clientId, int page, int size) {

        // Main logic with exceptions instead of ResponseEntity
        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if (clientOptional.isEmpty()) {
            throw new AccountException("Client not found");
        }

        Client client = clientOptional.get();
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findById(client.getIdShoppingCart());
        if (shoppingCartOptional.isEmpty()) {
            throw new ShoppingCartException("Shopping cart not found, grave error");
        }

        ShoppingCart shoppingCart = shoppingCartOptional.get();
        List<LoadLocalityOrdersForClient> localityOrdersList = new ArrayList<>();

        for (LocalityOrder localityOrder : shoppingCart.getLocalityOrders()) {
            Optional<Event> eventOptional = eventRepository.findById(localityOrder.getEventId());

            if (eventOptional.isEmpty()) {
                continue; // Skip orders for not found events
            }
            Event event = eventOptional.get();
            if (!LocalDateTime.now().plusDays(2).isBefore(event.getEventDate())) {
                continue; // Skip orders for events occurring soon
            }
            localityOrdersList.add(getLoadLocalityOrdersForClient(localityOrder, eventOptional));
        }

        // Pagination logic
        int totalElements = localityOrdersList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);

        if (startIndex >= totalElements) {
            return Map.of("message", "No locality orders", "content", List.of());
        }

        List<LoadLocalityOrdersForClient> paginatedOrders = localityOrdersList.subList(startIndex, endIndex);
        return Map.of(
                "totalPages", totalPages,
                "currentPage", page,
                "totalElements", totalElements,
                "content", paginatedOrders
        );
    }

    private static LoadLocalityOrdersForClient getLoadLocalityOrdersForClient(LocalityOrder localityOrder, Optional<Event> eventOptional) {
        Event event = eventOptional.get();
        return new LoadLocalityOrdersForClient(
                localityOrder.getPayingOrderId(), localityOrder.getNumTicketsSelected(),
                localityOrder.getLocalityName(), localityOrder.getTotalPaymentAmount(),
                event.getId(), event.getName(), event.getAddress(),
                event.getCity(), event.getEventDate(), event.getEventImageUrl(),
                event.getEventType());
    }

    @Override
    public void applyCoupon(String clientId, String couponName) {
        // Fetch the client
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new AccountException("Client not found"));

        // Fetch the shopping cart
        ShoppingCart clientShoppingCart = shoppingCartRepository.findById(client.getIdShoppingCart())
                .orElseThrow(() -> new ShoppingCartException("Shopping cart not found"));

        if (clientShoppingCart.isCouponClaimed()) {
            throw new LogicException("Only one coupon can be applied per shopping cart");
        }

        // Fetch the coupon
        Coupon coupon = couponRepository.findByName(couponName)
                .orElseThrow(() -> new LogicException("No coupon with this name found"));

        // Check for locality orders in shopping cart and if the total meets the minimum purchase requirement
        if (clientShoppingCart.getLocalityOrders().isEmpty()) {
            throw new LogicException("No ordered localities to apply a coupon");
        }

        Double totalPriceOfLocalityOrders = clientShoppingCart.getTotalPrice();
        if (totalPriceOfLocalityOrders < coupon.getMinPurchaseAmount()) {
            throw new PaymentException("Minimum purchase amount to use this coupon is " + coupon.getMinPurchaseAmount());
        }

        // Apply the discount to the total price
        double discountPercent = coupon.getDiscountPercent();
        Double totalPriceWithDiscount = totalPriceOfLocalityOrders * (1 - (discountPercent / 100));
        clientShoppingCart.setTotalPriceWithDiscount(totalPriceWithDiscount);
        clientShoppingCart.setCouponClaimed(true);
        clientShoppingCart.setAppliedDiscountPercent(1 - (discountPercent / 100));
        client.getListOfUsedCoupons().add(couponName);

        // Save changes
        clientRepository.save(client);
        shoppingCartRepository.save(clientShoppingCart);
    }

    @Override
    public ApiResponse<Map<String, Object>> loadPurchaseHistory(String clientId, int page, int size) {

        String verificationResponse = ClientSecurityUtils.verifyClientAccessWithId(clientId);
        if ("UNAUTHORIZED".equals(verificationResponse)) {
            throw new AccountException("Account not authorized");
        }

        try {
            List<Purchase> allPurchases = purchaseRepository.findByClientId(clientId); // Retrieve all purchases for the client

            // Calculate start and end indexes for the current page
            int start = Math.min(page * size, allPurchases.size());
            int end = Math.min(start + size, allPurchases.size());
            List<Purchase> paginatedPurchases = allPurchases.subList(start, end);

            // Prepare pagination details
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", paginatedPurchases);
            responseData.put("totalElements", allPurchases.size());
            responseData.put("totalPages", (int) Math.ceil((double) allPurchases.size() / size));
            responseData.put("currentPage", page);

            return new ApiResponse<>("Success", "Purchase history retrieved successfully", responseData);

        } catch (Exception e) {
            return new ApiResponse<>("Error", "Failed to retrieve purchase history", null);
        }
    }



}
