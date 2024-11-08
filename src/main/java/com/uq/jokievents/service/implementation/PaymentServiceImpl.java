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
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.PaymentService;
import com.uq.jokievents.service.interfaces.ShoppingCartService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ClientSecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
// TODO Implement this methods after doing the event payment logic
public class PaymentServiceImpl implements PaymentService {

    private final ShoppingCartService shoppingCartService;
    private final ClientRepository clientRepository;
    private final EventService eventService;
    private final ApplicationConfig applicationConfig;

    @Override
    public String doPayment(String clientId) {
        try {
            String verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
            if (verificationResponse.equals("UNAUTHORIZED")) {
                throw new NotAuthorizedException("Not authorized to enter this endpoint");
            }
            // Get the order from the database
            Optional<ShoppingCart> shoppingCartOptional = obtenerOrden(clientId);
            if (shoppingCartOptional.isEmpty()) {
                throw new ShoppinCartNotFoundException("The client does not have a shopping cart, grave error");
            }

            ShoppingCart shoppingCart = shoppingCartOptional.get();
            ArrayList<LocalityOrder> localityOrdersToBuy = shoppingCart.getLocalityOrders();
            if (localityOrdersToBuy.isEmpty()) {
                throw new EmptyShoppingCartException("The shopping cart is empty, nothing to buy");
            }

            Double discountPercentage = shoppingCart.getAppliedDiscountPercent();
            List<PreferenceItemRequest> itemsPasarela = new ArrayList<>();

            for (LocalityOrder localityOrder : localityOrdersToBuy) {
                Optional<Event> eventOptional = eventService.getEventById(localityOrder.getEventId());
                if (eventOptional.isEmpty()) continue;

                Event event = eventOptional.get();
                Locality locality = event.getLocalities(localityOrder.getLocalityName());

                if (locality == null) {
                    throw new LocalityNotFoundException("Locality within the " + event.getName() + " event, not found");
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
            shoppingCartService.saveShoppingCart(shoppingCart);

            // Only return the initPoint field
            return preference.getInitPoint();
        } catch (MPApiException e) {
            throw new PaymentProcessingException("Payment not done, API related issue: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            // todo I may want to add a logger to all of this
            throw new PaymentProcessingException("An unexpected error occurred while processing the payment.");
        }
    }

    private Optional<ShoppingCart> obtenerOrden(String clientId) {
        Optional<Client> optionalClient = clientRepository.findById(clientId);
        if (optionalClient.isEmpty()) {
            return Optional.empty();
        }
        Client client = optionalClient.get();
        String shoppingCartClientId = client.getIdShoppingCart();
        return shoppingCartService.findShoppingCartById(shoppingCartClientId);
    }

    @Override
    @Async
    public void receiveMercadopagoNotification(Map<String, Object> request) {
        try {
            Object type = request.get("type");

            if ("payment".equals(type)) {

                // "Capture" the json of the request
                String inputJson = request.get("data").toString();
                String paymentId = inputJson.replaceAll("\\D+", "");

                // Get mercadopago client and its payment
                PaymentClient client = new PaymentClient();
                Payment payment = client.get(Long.parseLong(paymentId));

                // Get orderId from metadata
                String orderId = payment.getMetadata().get("id_orden").toString();


                // Fetch the order from the database
                Optional<ShoppingCart> orderOpt = obtenerOrden(orderId);
                if (orderOpt.isEmpty()) return;
                ShoppingCart order = orderOpt.get();

                // Only proceed if the payment was successful
                // TODO The "approved" shall be implemented by us, somehow. Ask Rojo!
                // todo make the approved empty the client shopping cart and add it to the buying history (buying history attribute must be added to the Client model class)
                if ("approved".equals(payment.getStatus())) {
                    // For each locality ordered, update the corresponding event and locality capacity
                    for (LocalityOrder localityOrder : order.getLocalityOrders()) {
                        Optional<Event> eventOpt = eventService.getEventById(localityOrder.getEventId());
                        if (eventOpt.isEmpty()) continue;

                        Event event = eventOpt.get();
                        Locality locality = event.getLocalities(localityOrder.getLocalityName());
                        if (locality == null) continue;

                        // Update the available seats
                        locality.setMaxCapacity(locality.getMaxCapacity() - localityOrder.getNumTicketsSelected());
                        event.setTotalAvailablePlaces(event.getTotalAvailablePlaces() - localityOrder.getNumTicketsSelected());

                        // Save the event with updated capacities
                        eventService.saveEvent(event);
                    }

                    // Save payment details to the order and update order status
                    OrderPayment orderPayment = createPayment(payment);
                    order.setOrderPayment(orderPayment);
                    shoppingCartService.saveShoppingCart(order);
                }
            }
        } catch ( MPException e) {
            throw new PaymentProcessingException("An unexpected error occurred while processing the payment of mercadopago.");
        } catch (MPApiException e) {
            throw new PaymentProcessingException("Payment notification error, API related issue: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            throw new PaymentProcessingException("An unexpected error occurred while processing the payment.");
        }
    }

    private OrderPayment createPayment(Payment payment) {
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setId(payment.getId().toString());
        orderPayment.setPaymentDate( payment.getDateCreated().toLocalDateTime());
        orderPayment.setPaymentStatus(payment.getStatus());
        orderPayment.setPaymentStatus(payment.getStatusDetail());
        orderPayment.setPaymentType(payment.getPaymentTypeId());
        orderPayment.setPaymentCurrency(payment.getCurrencyId());
        orderPayment.setAuthorizationCode(payment.getAuthorizationCode());
        orderPayment.setPaymentValue(payment.getTransactionAmount().floatValue());
        return orderPayment;
    }
}
