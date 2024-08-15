package com.uq.jokievents;

import com.mongodb.client.vault.ClientEncryption;
import com.uq.jokievents.controller.ClientController;
import com.uq.jokievents.model.Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;

@SpringBootApplication
public class JokieventsApplication {

	/**
	 * Starter method for the server
	 * @param args
	 */
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(JokieventsApplication.class, args);

		// Obtener el bean del controlador ClientController
		ClientController contrClient = context.getBean(ClientController.class);

		// Creando una lista de cupones (idCoupons)
		ArrayList<String> idCoupons = new ArrayList<>();
		idCoupons.add("COUPON123");
		idCoupons.add("COUPON456");

		// Creando un objeto de Client con los datos proporcionados
		Client client = new Client(
				"123456789",                // idCard
				"John Doe",                 // name
				"123 Main St",              // direction
				"555-1234",                 // phoneNumber
				"john.doe@example.com",     // email
				"password123",              // password
				idCoupons,                  // idCoupons
				"SHOPPING_CART_ID"          // shoppingCart
		);

		// Usa el controlador para crear el cliente en la base de datos
		contrClient.createClient(client);

		System.out.println("Client created successfully!");
	}


}
