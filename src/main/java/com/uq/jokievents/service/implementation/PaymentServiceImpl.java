package com.uq.jokievents.service.implementation;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import com.uq.jokievents.config.ApplicationConfig;
import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.model.*;
import com.uq.jokievents.repository.ClientRepository;
import com.uq.jokievents.repository.EventRepository;
import com.uq.jokievents.repository.PurchaseRepository;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.ImageService;
import com.uq.jokievents.service.interfaces.PaymentService;
import com.uq.jokievents.utils.ClientSecurityUtils;
import com.uq.jokievents.utils.EmailService;
import com.uq.jokievents.utils.Generators;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ClientRepository clientRepository;
    private final PurchaseRepository purchaseRepository;
    private final ImageService imageService;
    private final EmailService emailService;
    private final ApplicationConfig applicationConfig;
    private final EventRepository eventRepository;

    @Override
    public String doPayment(String clientId) {
        try {
            String verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
            if (verificationResponse.equals("UNAUTHORIZED")) {
                throw new AuthorizationException("Not authorized to enter this endpoint");
            }
            // Get the order from the database
            Optional<ShoppingCart> shoppingCartOptional = obtenerOrden(clientId);
            if (shoppingCartOptional.isEmpty()) {
                throw new AccountException("The client does not have a shopping cart, grave error");
            }

            ShoppingCart shoppingCart = shoppingCartOptional.get();
            ArrayList<LocalityOrder> localityOrdersToBuy = shoppingCart.getLocalityOrders();
            if (localityOrdersToBuy.isEmpty()) {
                throw new ShoppingCartException("The shopping cart is empty, nothing to buy");
            }

            Double discountPercentage = shoppingCart.getAppliedDiscountPercent();
            List<PreferenceItemRequest> itemsPasarela = new ArrayList<>();

            for (LocalityOrder localityOrder : localityOrdersToBuy) {
                Optional<Event> eventOptional = eventRepository.findById(localityOrder.getEventId());
                if (eventOptional.isEmpty()) continue;

                Event event = eventOptional.get();
                Locality locality = event.getLocalities(localityOrder.getLocalityName());

                if (locality == null) {
                    throw new EventException("Locality within the " + event.getName() + " event, not found");
                }

                // todo guess why the fuck only the price shows on the payment
                PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                        .id(event.getId())
                        .title(event.getName())
                        .pictureUrl(event.getEventImageUrl())
                        .categoryId(event.getEventType().name())
                        .quantity(localityOrder.getNumTicketsSelected())
                        .currencyId("COP")
                        .unitPrice(BigDecimal.valueOf(locality.getPrice() * discountPercentage))
                        .build();

                itemsPasarela.add(itemRequest);
            }

            MercadoPagoConfig.setAccessToken(applicationConfig.getAccessToken());

            // todo do we have to assign this urls?
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("URL PAGO EXITOSO")
                    .failure("URL PAGO FALLIDO")
                    .pending("URL PAGO PENDIENTE")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .backUrls(backUrls)
                    .items(itemsPasarela)
                    .metadata(Map.of("id_orden", shoppingCart.getId()))
                    .notificationUrl("http://localhost:8080/api/payment/receive-payment-confirmation")
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            shoppingCart.setPaymentGatewayId(preference.getId());
            shoppingCartRepository.save(shoppingCart);

            // Only return the initPoint field
            return preference.getInitPoint();
        } catch (MPApiException e) {
            throw new PaymentException("Payment not done, API related issue: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            // todo I may want to add a logger to all of this
            throw new PaymentException("An unexpected error occurred while processing the payment.");
        }
    }

    private Optional<ShoppingCart> obtenerOrden(String clientId) {
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isEmpty()) {
            return Optional.empty();
        }
        Client client = optionalClient.get();
        String shoppingCartClientId = client.getIdShoppingCart();
        return shoppingCartRepository.findById(shoppingCartClientId);
    }

    @Override
    @Async
    public void receiveMercadopagoNotification(Map<String, Object> request) {
        try {
            Object type = request.get("type");

            if ("payment".equals(type)) {

                String inputJson = request.get("data").toString();
                String paymentId = inputJson.replaceAll("\\D+", "");

                PaymentClient client = new PaymentClient();
                Payment payment = client.get(Long.parseLong(paymentId));

                String orderId = payment.getMetadata().get("id_orden").toString();
                Optional<ShoppingCart> orderOpt = obtenerOrden(orderId);
                if (orderOpt.isEmpty()) return;

                ShoppingCart order = orderOpt.get();

                // Check if the payment status is "approved"
                if ("approved".equals(payment.getStatus())) {
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

                        // Decrease the total available places of the event by the number of selected tickets
                        event.setTotalAvailablePlaces(event.getTotalAvailablePlaces() - localityOrder.getNumTicketsSelected());

                        // Save the updated event back to the repository
                        eventRepository.save(event);
                    }

                    OrderPayment orderPayment = createPayment(payment);
                    order.setOrderPayment(orderPayment);
                    shoppingCartRepository.save(order);

                    // Create and save the purchase
                    Purchase purchase = Purchase.builder()
                            .clientId(order.getIdClient())
                            .purchaseDate(LocalDateTime.now())
                            .purchasedItems(new ArrayList<>(order.getLocalityOrders()))
                            .totalAmount(orderPayment.getPaymentValue())
                            .paymentMethod(payment.getPaymentTypeId())
                            .build();

                    purchaseRepository.save(purchase);

                    // Clear the shopping cart for the client
                    order.setLocalityOrders(new ArrayList<>());
                    order.setTotalPrice(0.0);
                    order.setTotalPriceWithDiscount(0.0);
                    order.setPaymentCoupon(null);
                    order.setAppliedDiscountPercent(null);
                    order.setCouponClaimed(false);
                    shoppingCartRepository.save(order);

                    // Send the purchase QR code to the client
                    Client purchaseClient = clientRepository.findById(order.getIdClient()).orElseThrow(() -> new RuntimeException("Client not found"));
                    sendPurchaseQRCodeEmail(purchaseClient, purchase);
                }
            }
        } catch (MPException e) {
            throw new PaymentException("An unexpected error occurred while processing the payment of mercadopago.");
        } catch (MPApiException e) {
            throw new PaymentException("Payment notification error, API related issue: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            throw new PaymentException("An unexpected error occurred while processing the payment.");
        }
    }

    private OrderPayment createPayment(Payment payment) {
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setId(payment.getId().toString());
        orderPayment.setPaymentDate(payment.getDateCreated().toLocalDateTime());
        orderPayment.setPaymentStatus(payment.getStatus());
        orderPayment.setPaymentStatus(payment.getStatusDetail());
        orderPayment.setPaymentType(payment.getPaymentTypeId());
        orderPayment.setPaymentCurrency(payment.getCurrencyId());
        orderPayment.setAuthorizationCode(payment.getAuthorizationCode());
        orderPayment.setPaymentValue(payment.getTransactionAmount());
        return orderPayment;
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
