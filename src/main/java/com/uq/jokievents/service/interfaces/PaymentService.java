package com.uq.jokievents.service.interfaces;

import com.braintreepayments.http.HttpResponse;
import com.paypal.orders.Capture;
import com.paypal.orders.Order;
import com.uq.jokievents.model.ShoppingCart;

public interface PaymentService {

    HttpResponse<Order> createPaymentOrder(String clientId) throws Exception;
    Capture capturePayment(String orderId) throws Exception;
    void fillPurchaseAfterSuccess(ShoppingCart order);
    void updateEventAndLocalities(ShoppingCart order);
    ShoppingCart getShoppingCart(String clientId);
}
