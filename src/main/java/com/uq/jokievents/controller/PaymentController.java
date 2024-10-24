package com.uq.jokievents.controller;

import com.uq.jokievents.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @PostMapping("/pay-shoppingcart/{shoppingCartId}")
    public ResponseEntity<?> payShoppingCart(@PathVariable String clientId) throws Exception {
        return paymentService.doPayment(clientId);
    }

    @PostMapping("/receive-payment-confirmation")
    public void payShoppingCart(@RequestParam Map<String, Object> request) {
        paymentService.receiveMercadopagoNotification(request);
    }
}


