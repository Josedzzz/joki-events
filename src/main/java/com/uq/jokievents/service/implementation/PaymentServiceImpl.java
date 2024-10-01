package com.uq.jokievents.service.implementation;

import com.mercadopago.client.preference.PreferenceItemRequest;
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


    @Override
    public Preference doPayment(String orderId, String clientId) throws Exception {

        // Creo que es mejor añadir un eventId a LocalityOrder. No se manejan órdenes en sí, se podría implementar con algo que combine el ShoppingCart y TicketOrder (localidades + hora)
        // Creo que es mejor hacer los pagos con PayU y es válido según el proyecto.

//        // Obtener la orden guardada en la base de datos y los ítems de la orden
//        Orden ordenGuardada = obtenerOrden(idOrden);
//        List<PreferenceItemRequest> itemsPasarela = new ArrayList<>();
//
//
//        // Recorrer los items de la orden y crea los ítems de la pasarela
//        for(DetalleOrden item : ordenGuardada.getDetalle()){
//
//
//            // Obtener el evento y la localidad del ítem
//            Evento evento = eventoServicio.obtenerEvento(item.getCodigoEvento().toString());
//            Localidad localidad = evento.obtenerLocalidad(item.getNombreLocalidad());
//
//
//            // Crear el item de la pasarela
//            PreferenceItemRequest itemRequest =
//                    PreferenceItemRequest.builder()
//                            .id(evento.getCodigo())
//                            .title(evento.getNombre())
//                            .pictureUrl(evento.getImagenPortada())
//                            .categoryId(evento.getTipo().name())
//                            .quantity(item.getCantidad())
//                            .currencyId("COP")
//                            .unitPrice(BigDecimal.valueOf(localidad.getPrecio()))
//                            .build();
//
//
//            itemsPasarela.add(itemRequest);
//        }
//
//
//        // Configurar las credenciales de MercadoPago
//        MercadoPagoConfig.setAccessToken("ACCESS_TOKEN");
//
//
//        // Configurar las urls de retorno de la pasarela (Frontend)
//        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
//                .success("URL PAGO EXITOSO")
//                .failure("URL PAGO FALLIDO")
//                .pending("URL PAGO PENDIENTE")
//                .build();
//
//
//        // Construir la preferencia de la pasarela con los ítems, metadatos y urls de retorno
//        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
//                .backUrls(backUrls)
//                .items(itemsPasarela)
//                .metadata(Map.of("id_orden", ordenGuardada.getCodigo()))
//                .notificationUrl("URL NOTIFICACION")
//                .build();
//
//
//        // Crear la preferencia en la pasarela de MercadoPago
//        PreferenceClient client = new PreferenceClient();
//        Preference preference = client.create(preferenceRequest);
//
//
//        // Guardar el código de la pasarela en la orden
//        ordenGuardada.setCodigoPasarela( preference.getId() );
//        ordenRepo.save(ordenGuardada);
//
//
//        return preference;
        return null;
    }

    @Override
    public void receiveMercadopagoNotification(Map<String, Object> request) {

    }
}
