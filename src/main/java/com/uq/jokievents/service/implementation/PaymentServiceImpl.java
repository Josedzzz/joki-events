package com.uq.jokievents.service.implementation;

import com.mercadopago.resources.preference.Preference;
import com.uq.jokievents.service.interfaces.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
// TODO Implement this methods after doing the event payment logic
public class PaymentServiceImpl implements PaymentService {


    @Override
    public Preference doPayment(String orderId) throws Exception {
        return null;
    }

    @Override
    public void receiveMercadopagoNotification(Map<String, Object> request) {

    }
}
