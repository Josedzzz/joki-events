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
import com.uq.jokievents.model.*;
import com.uq.jokievents.service.interfaces.EventService;
import com.uq.jokievents.service.interfaces.PaymentService;
import com.uq.jokievents.service.interfaces.ShoppingCartService;
import com.uq.jokievents.utils.ApiResponse;
import com.uq.jokievents.utils.ClientSecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final EventService eventService;
    private final ApplicationConfig applicationConfig;

    @Override
    public ResponseEntity<?> doPayment(String shoppingCartID) {
        try {
            ResponseEntity<?> verificationResponse = ClientSecurityUtils.verifyClientAccessWithRole();
            if (verificationResponse != null) {
                return verificationResponse;
            }

            // Get the order from the database
            Optional<ShoppingCart> shoppingCartOptional = obtenerOrden(shoppingCartID);
            if (shoppingCartOptional.isEmpty()) return null;
            ShoppingCart shoppingCart = shoppingCartOptional.get();
            // Will be 1 or a percentage given by a Coupon (shakira)
            // had to sort to this solution as mercadopago does not allow to show a total price
            Double discountPercentage = shoppingCart.getAppliedDiscountPercent();

            List<PreferenceItemRequest> itemsPasarela = new ArrayList<>();


            // Recorrer los items de la orden y crea los ítems de la pasarela
            for(LocalityOrder localityOrder : shoppingCart.getLocalityOrders()){

                // Obtener el evento y la localidad del ítem
                Optional<Event> eventOptional = eventService.getEventById(localityOrder.getEventId());
                if(eventOptional.isEmpty()) continue;

                Event event = eventOptional.get();
                Locality locality = event.getLocalities(localityOrder.getLocalityName());
                assert locality != null;

                // Crear el item de la pasarela

                PreferenceItemRequest itemRequest =
                        PreferenceItemRequest.builder()
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

            // Configurar las credenciales de MercadoPago
            MercadoPagoConfig.setAccessToken(applicationConfig.getAccessToken()); // Later will do

            // Configurar las urls de retorno de la pasarela (Frontend)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("URL PAGO EXITOSO")
                    .failure("URL PAGO FALLIDO")
                    .pending("URL PAGO PENDIENTE")
                    .build();


            // Construir la preferencia de la pasarela con los ítems, metadatos y urls de retorno
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .backUrls(backUrls)
                    .items(itemsPasarela)
                    .metadata(Map.of("id_orden", shoppingCart.getId()))
                    .notificationUrl("http://localhost:8080/api/payment/receive-payment-confirmation") // What should this have?
                    .build();


            // Crear la preferencia en la pasarela de MercadoPago
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Guardar el código de la pasarela en la orden
            shoppingCart.setPaymentGatewayId(preference.getId());
            shoppingCartService.saveShoppingCart(shoppingCart);

            ApiResponse<?> response = new ApiResponse<>("Success", "Payment done", preference);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (MPApiException e) {
            System.out.println("MercadoPago API error response: " + e.getApiResponse().getContent());
            ApiResponse<?> response = new ApiResponse<>("Error", "Payment not done, API RELATED", e.getApiResponse().getContent());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (Exception e) {
            ApiResponse<?> response = new ApiResponse<>("Error", "Payment not done", e.toString());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Optional<ShoppingCart> obtenerOrden(String shoppingCartID) {
        return shoppingCartService.findShoppingCartById(shoppingCartID);
    }

    @Override
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
        } catch (MPException | MPApiException e) {
            throw new RuntimeException(e);
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
