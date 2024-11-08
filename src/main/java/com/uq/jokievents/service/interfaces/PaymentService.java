package com.uq.jokievents.service.interfaces;

import com.mercadopago.resources.preference.Preference;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PaymentService {

    String doPayment(String clientId);
    void receiveMercadopagoNotification(Map<String, Object> request);
}
