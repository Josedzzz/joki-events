package com.uq.jokievents.dtos;

import com.uq.jokievents.model.LocalityOrder;
import java.util.ArrayList;

public record ShoppingCartSaveDTO(

        String idClient,

        ArrayList<LocalityOrder> localityOrders,

        Double totalPrice // Price of all the localities a client may have
) {}
