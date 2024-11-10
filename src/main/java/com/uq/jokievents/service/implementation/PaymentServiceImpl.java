package com.uq.jokievents.service.implementation;

import com.braintreepayments.http.HttpResponse;
import com.braintreepayments.http.exceptions.HttpException;

import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.*;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.*;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.repository.PurchaseRepository;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.ImageService;
import com.uq.jokievents.service.interfaces.PaymentService;
import org.springframework.stereotype.Service;
import com.uq.jokievents.utils.EmailService;
import com.uq.jokievents.utils.Generators;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final PurchaseRepository purchaseRepository;
    private final ImageService imageService;
    private final EmailService emailService;
    private final EventRepository eventRepository;
    private final PayPalHttpClient payPalHttpClient;
    private final ClientRepository clientRepository;

    @Override
    public HttpResponse<Order> createPaymentOrder(String clientId) throws Exception{

        // Get the order from the database

        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if (clientOptional.isEmpty()) {
            throw new AccountException("Client does not exists, weird");
        }
        Client payingClient = clientOptional.get();

        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findById(payingClient.getIdShoppingCart());
        if (shoppingCartOptional.isEmpty()) {
            throw new AccountException("The client does not have a shopping cart, grave error");
        }
        // Have a shopping cart and a Purchase
        ShoppingCart shoppingCart = shoppingCartOptional.get();
        Purchase purchase = new Purchase();

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(buildOrderRequest(purchase.getId(), shoppingCart));
        try {
            HttpResponse<Order> response = payPalHttpClient.execute(request);
            System.out.println("Order ID: " + response.result().id());
            return response;
        } catch (HttpException e) {
            System.err.println("Failed to create order: " + e.getMessage());
            throw e;
        }
    }

    private OrderRequest buildOrderRequest(String referenceId, ShoppingCart shoppingCart) {
        // Create a new OrderRequest
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.intent("CAPTURE");

        // Set the application context (for URLs)
        ApplicationContext applicationContext = new ApplicationContext()
                .cancelUrl("http://localhost:8080/api/payment/cancel")
                .returnUrl("http://localhost:8080/api/payment/success");
        orderRequest.applicationContext(applicationContext);

        // Create the purchase unit (order item)
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .referenceId(referenceId)
                .amount(new AmountWithBreakdown()
                        .currencyCode("USD")
                        .value(shoppingCart.getTotalPriceWithDiscount().toString()));  // Use the rounded value

        // Add the purchase unit to the order request
        orderRequest.purchaseUnits(List.of(purchaseUnitRequest));

        return orderRequest;
    }

    // I could make this message a thousand times better, but I am tired
    private String buildOrderDescription(ShoppingCart shoppingCart) {
        StringBuilder description = new StringBuilder();
        // Append locality details in a more compact format
        for (LocalityOrder localityOrder : shoppingCart.getLocalityOrders()) {
            description.append(" | ").append(localityOrder.getNumTicketsSelected())
                    .append("x ").append(localityOrder.getLocalityName());
        }

        return description.toString();
    }

    @Override
    public Capture capturePayment(String orderId) throws Exception {
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.requestBody(new OrderRequest());  // Typically an empty request body for capture

        HttpResponse<Order> response = payPalHttpClient.execute(request);

        if ("COMPLETED".equals(response.result().status())) {
            // Return the capture details
            return response.result().purchaseUnits().get(0).payments().captures().get(0);
        }

        throw new Exception("Payment capture failed.");
    }

    @Override
    public void fillPurchaseAfterSuccess(ShoppingCart order) {
            Purchase purchase = new Purchase();
            purchase.setPurchaseDate(LocalDateTime.now());
            purchase.setPaymentMethod("PayPal");
            purchase.setTotalAmount(BigDecimal.valueOf(order.getTotalPriceWithDiscount()));
            List<LocalityOrder> purchasedItems = new ArrayList<>(order.getLocalityOrders());
            purchase.setPurchasedItems(purchasedItems);
            purchaseRepository.save(purchase);

            order.setPaymentGatewayId("");
            order.setLocalityOrders(new ArrayList<>());
            order.setTotalPrice(0.0);
            order.setTotalPriceWithDiscount(0.0);
            order.setAppliedDiscountPercent(1.0);
            order.setCouponClaimed(false);
            shoppingCartRepository.save(order);  // Save the updated shopping cart with cleared items

            Optional<Client> clientOptional = clientRepository.findById(order.getClientId());
            if (clientOptional.isEmpty()) {
                throw new AccountException("No account to send the payment receipt");
            }
            Client client = clientOptional.get();
            sendPurchaseQRCodeEmail(client, purchase);
    }

     @Override
    public void updateEventAndLocalities(ShoppingCart order) {
        // Iterate through each LocalityOrder in the order
        for (LocalityOrder localityOrder : order.getLocalityOrders()) {
            // Retrieve the event associated with the localityOrder using its event ID
            Optional<Event> eventOpt = eventRepository.findById(localityOrder.getEventId());

            // If no event is found for the given ID, skip the current iteration
            if (eventOpt.isEmpty()) continue;

            // Get the event object from the Optional
            Event event = eventOpt.get();

            // Retrieve the specific locality from the event based on the locality name
            Locality locality = event.getLocalities(localityOrder.getLocalityName());

            // If no locality is found for the given locality name, skip the current iteration
            if (locality == null) continue;

            // Decrease the maximum capacity of the locality by the number of selected tickets
            locality.setMaxCapacity(locality.getMaxCapacity() - localityOrder.getNumTicketsSelected());
            locality.setCurrentOccupancy(locality.getCurrentOccupancy() + localityOrder.getNumTicketsSelected());

            // Decrease the total available places of the event by the number of selected tickets
            event.setTotalAvailablePlaces(event.getTotalAvailablePlaces() - localityOrder.getNumTicketsSelected());

            // Save the updated event back to the repository
            eventRepository.save(event);
        }
    }

    @Override
    public ShoppingCart getShoppingCart(String clientId) {

        Optional<Client> clientOptional = clientRepository.findById(clientId);
        if (clientOptional.isEmpty()) {
            throw new AccountException("Not an existing client to pay");
        }
        Client client = clientOptional.get();
        String shoppingCartId = client.getIdShoppingCart();
        Optional<ShoppingCart> shoppingCartOptional = shoppingCartRepository.findById(shoppingCartId);
        if (shoppingCartOptional.isEmpty()) {
            throw new AccountException("Client does not have a shopping cart, grave error");
        }
        return shoppingCartOptional.get();
    }

    private void sendPurchaseQRCodeEmail(Client client, Purchase purchase) {
        try {
            // Generate the QR code from purchase details (e.g., purchase ID, amount, etc.)
            String qrCodeData = "Purchase ID: " + purchase.getId() + "\nTotal Amount: " + purchase.getTotalAmount();
            String base64QRCode = Generators.generateQRCode(qrCodeData);

            // Upload the QR code image to Firebase Storages
            String qrCodeUrl = imageService.uploadImage(base64QRCode);

            // Send the email with the QR code URL (use your email service to send this)
            String emailBody = "Dear " + client.getName() + ",\n\nHere is your purchase QR code:\n" + qrCodeUrl;
            emailService.sendPurchaseEmail(client.getEmail(), "Your Purchase QR Code", emailBody);
        } catch (Exception e) {
            throw new LogicException(e.getMessage());
        }
    }
}
