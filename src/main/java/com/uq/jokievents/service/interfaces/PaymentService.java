package com.uq.jokievents.service.interfaces;

import com.mercadopago.resources.preference.Preference;

import java.util.Map;

public interface PaymentService {

    Preference doPayment(String orderId) throws Exception;
    void receiveMercadopagoNotification(Map<String, Object> request);
}
