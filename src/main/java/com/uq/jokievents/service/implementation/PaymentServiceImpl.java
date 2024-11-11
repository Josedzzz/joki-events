package com.uq.jokievents.service.implementation;

import com.braintreepayments.http.HttpResponse;
import com.braintreepayments.http.exceptions.HttpException;

import com.paypal.core.PayPalHttpClient;
import com.paypal.orders.*;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.*;
import com.uq.jokievents.model.Event;
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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

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
    public HttpResponse<Order> createPaymentOrder(String clientId){

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

        if (shoppingCart.getLocalityOrders().isEmpty()) {
            throw new LogicException("Nothing to pay in the shopping cart");
        }

        Purchase purchase = new Purchase();

        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(buildOrderRequest(purchase.getId(), shoppingCart));
        try {
            return payPalHttpClient.execute(request);
        } catch (IOException e) {
            throw new AccountException("Could not generate the payment link");
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
                .description(buildOrderDescription(shoppingCart))
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
            purchase.setClientId(order.getClientId());
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

            // Increase the current occupancy of the locality by the number of selected tickets
            locality.setCurrentOccupancy(locality.getCurrentOccupancy() + localityOrder.getNumTicketsSelected());

            // Decrease the total available places of the event by the number of selected tickets
            event.setTotalAvailablePlaces(event.getTotalAvailablePlaces() - localityOrder.getNumTicketsSelected());

            if (event.getTotalAvailablePlaces() == 0) {
                event.setAvailableForPurchase(false);
            }
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
            // What I basically think this does is create an image with the information of the purchase alone, then I convert that image to Base64 and upload it to Firebase, then I get that firebase link and create a QR Code that contains the information of that link so that when scanned it shows the purchase info image
            BufferedImage customImage = createCustomPurchaseImage(client, purchase);
            String base64QRCode = encodeImageToBase64(customImage);
            // todo make that shit a constant
            String qrCodeFirebaseUrl = imageService.uploadImage("data:image/png;base64,"+base64QRCode);

            String qrCodeImageBase64 = Generators.generateQRCode(qrCodeFirebaseUrl);

            // Send the email with the embedded image (QR code)
            String emailBody = "<p>Dear " + client.getName() + ",</p>" +
                    "<p>Here is your purchase QR code with details:</p>" +
                    "<img src='cid:qrCodeImage' />"; // This references the embedded image using its Content-ID
            emailService.sendPurchaseEmail(client.getEmail(), "Your Purchase QR Code", emailBody, qrCodeImageBase64);
        } catch (Exception e) {
            throw new LogicException(e.getMessage());
        }
    }

    private String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private BufferedImage createCustomPurchaseImage(Client client, Purchase purchase) {
        int width = 600; // Image width
        int height = 600; // Increased height for more content
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill background with white color
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Set text color and font for purchase details
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 14));

        // Draw the purchase details
        int y = 30;  // Starting y-position for text

        // Purchase summary
        g2d.drawString("Purchase ID: " + purchase.getId(), 20, y);
        y += 20;
        g2d.drawString("Client: " + client.getName(), 20, y);
        y += 20;
        g2d.drawString("Total Amount: $" + purchase.getTotalAmount(), 20, y);
        y += 20;
        g2d.drawString("Payment Method: " + purchase.getPaymentMethod(), 20, y);
        y += 20;
        g2d.drawString("Purchase Date: " + purchase.getPurchaseDate(), 20, y);
        y += 40;

        // Locality order details
        g2d.drawString("Purchased Items:", 20, y);
        y += 20;
        for (LocalityOrder order : purchase.getPurchasedItems()) {
            // todo check if the business logic will ever delete an event from the database, for now, assume not
            Optional<Event> eventOptional = eventRepository.findById(order.getEventId());
            if (eventOptional.isEmpty()) continue;
            Event event = eventOptional.get();

            g2d.drawString("     Event name: " + event.getName(), 20, y);
            y += 20;
            g2d.drawString("     Event address: " + event.getAddress(), 20, y);
            y += 20;
            g2d.drawString("     Date of the event: " + event.getEventDate(), 20, y);
            y += 20;
            g2d.drawString("     Locality: " + order.getLocalityName(), 20, y);
            y += 20;
            g2d.drawString("     Tickets: " + order.getNumTicketsSelected(), 20, y);
            y += 20;
            g2d.drawString("     Total: $" + order.getTotalPaymentAmount(), 20, y);
            y += 30;  // Adding extra space between orders klasjdhjkgaksd
        }

        // Dispose the graphics context and return the image
        g2d.dispose();
        return image;
    }

}
