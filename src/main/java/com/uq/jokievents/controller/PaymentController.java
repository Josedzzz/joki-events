package com.uq.jokievents.controller;

import com.braintreepayments.http.HttpResponse;
import com.paypal.orders.Capture;
import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.repository.ShoppingCartRepository;
import com.uq.jokievents.service.interfaces.PaymentService;
import com.uq.jokievents.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ShoppingCartRepository shoppingCartRepository;

    @PostMapping("/{clientId}/create-payment")
    public ResponseEntity<ApiResponse<String>> createOrder(@PathVariable String clientId) {
        try {
            HttpResponse<Order> orderHttpResponse = paymentService.createPaymentOrder(clientId);

            String orderId = orderHttpResponse.result().id();
            ShoppingCart shoppingCart = paymentService.getShoppingCart(clientId);
            shoppingCart.setPaymentGatewayId(orderId);
            shoppingCartRepository.save(shoppingCart);

            Order order = orderHttpResponse.result();
            String approvalUrl = null;

            // Iterate over the links to find the approval URL
            for (LinkDescription link : order.links()) {
                if ("approve".equals(link.rel())) {
                    approvalUrl = link.href();
                    break;
                }
            }
            if (approvalUrl != null) {
                ApiResponse<String> response = new ApiResponse<>("Success", "Redirecting you to the payment link", approvalUrl);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Error", "Could not load a payment link", null);
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/success")
    public ResponseEntity<ApiResponse<String>> handlePaymentSuccess(@RequestParam("token") String token) {
        try {
            // Step 1: Capture the payment
            Capture capture = paymentService.capturePayment(token);

            // Step 2: Check if payment status is 'COMPLETED'
            if ("COMPLETED".equals(capture.status())) {
                // Step 3: Retrieve the ShoppingCart based on the payment token
                Optional<ShoppingCart> order = shoppingCartRepository.findByPaymentGatewayId(token);

                if (order.isEmpty()) {
                    ApiResponse<String> response = new ApiResponse<>("Error", "Could not find the order", capture.status());
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                }
                paymentService.fillPurchaseAfterSuccess(order.get());
                paymentService.updateEventAndLocalities(order.get());
                ApiResponse<String> response = new ApiResponse<>("Success", "Payment done", null);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse<String> response = new ApiResponse<>("Success", "Payment done, probably cancelled", capture.status());
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>("Error", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Qué pesar este método kjaskjaksjkajs
    @GetMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> handlePaymentCancel() {
        ApiResponse<String> response = new ApiResponse<>("Success", "Payment cancelled", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}


