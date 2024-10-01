package com.uq.jokievents.service.implementation;

import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.preference.PreferenceItem;
import com.mercadopago.resources.preference.PreferencePayer;
import com.uq.jokievents.model.Client;
import com.uq.jokievents.model.LocalityOrder;
import com.uq.jokievents.model.ShoppingCart;
import com.uq.jokievents.service.interfaces.ClientService;
import com.uq.jokievents.service.interfaces.PaymentService;
import com.uq.jokievents.service.interfaces.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
// TODO Implement this methods after doing the event payment logic
public class PaymentServiceImpl implements PaymentService {

    private final ShoppingCartService shoppingCartService;
    private final ClientService clientService;

    static {
        // Initialize MercadoPago SDK with your credentials (access token). XD
        // MercadoPago.SDK.setAccessToken("YOUR_ACCESS_TOKEN");
    }

    @Override
    public Preference doPayment(String orderId, String clientId) throws Exception {
        // Fetch the client's shopping cart
        ShoppingCart shoppingCart = shoppingCartService.findShoppingCartById(clientId).orElseThrow(() -> new Exception("Shopping cart not found"));
        Client client = clientService.findClientById(clientId).orElseThrow(() -> new Exception("Client not found"));

        // Create a preference object
        Preference preference = new Preference();

        // Add items to the preference
        List<PreferenceItem> items = new ArrayList<>();
        for (LocalityOrder localityOrder : shoppingCart.getLocalityOrders()) {
            PreferenceItem item = new PreferenceItem();

            // Using reflection to set the fields for PreferenceItem
            setPreferenceItemField(item, "title", localityOrder.getLocalityName());
            setPreferenceItemField(item, "quantity", localityOrder.getNumTicketsSelected());
            setPreferenceItemField(item, "unitPrice", BigDecimal.valueOf(localityOrder.getTotalPaymentAmount()));

            // Add the item to the list
            items.add(item);
        }

        // Set items in preference
        setPreferenceField(preference, "items", items);

        // Set payer information
        PreferencePayer payer = new PreferencePayer();
        setPreferencePayerField(payer, client.getEmail());
        // Optionally set name and surname if available, I may use this.
        // setPreferencePayerField(payer, "name", client.getFirstName());
        // setPreferencePayerField(payer, "surname", client.getLastName());

        // Set payer in preference
        setPreferenceField(preference, "payer", payer);

        // Set additional fields in preference as needed
        setPreferenceField(preference, "clientId", clientId);
        // Set other fields like paymentMethods, backUrls, etc. here

        // Save and return the payment preference
        return preference;
    }

    // Helper method to set fields using reflection for PreferencePayer
    private void setPreferencePayerField(PreferencePayer payer, Object value) {
        try {
            Field field = PreferencePayer.class.getDeclaredField("email");
            field.setAccessible(true);
            field.set(payer, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.getMessage();
        }
    }

    // (Existing) Helper method for PreferenceItem
    private void setPreferenceItemField(PreferenceItem item, String fieldName, Object value) {
        try {
            Field field = PreferenceItem.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(item, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.getMessage();
        }
    }

    // (Existing) Helper method for Preference
    private void setPreferenceField(Preference preference, String fieldName, Object value) {
        try {
            Field field = Preference.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(preference, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.getMessage();
        }
    }


    @Override
    public void receiveMercadopagoNotification(Map<String, Object> request) {

    }
}
