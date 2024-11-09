package com.uq.jokievents.controller;

import com.uq.jokievents.exceptions.*;
import com.uq.jokievents.service.interfaces.PaymentService;
import com.uq.jokievents.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{clientId}/pay-shopping-cart")
    public ResponseEntity<ApiResponse<String>> payShoppingCart(@PathVariable String clientId) {
        try {
            String initPoint = paymentService.doPayment(clientId);
            return ResponseEntity.ok(new ApiResponse<>("Success", "Payment done", initPoint));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>("Error", e.getMessage(), null));
        }
    }

    @PostMapping("/receive-payment-confirmation")
    public ResponseEntity<String> payShoppingCart(@RequestBody Map<String, Object> request) {
        try {
            paymentService.receiveMercadopagoNotification(request);
            return ResponseEntity.ok("Notification received successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}


