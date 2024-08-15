package com.uq.jokievents;

import com.mongodb.client.vault.ClientEncryption;
import com.uq.jokievents.controller.AdminController;
import com.uq.jokievents.controller.ClientController;
import com.uq.jokievents.model.Admin;
import com.uq.jokievents.model.Client;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class JokieventsApplication {

	/**
	 * Starter method for the server
	 * @param args
	 */
	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(JokieventsApplication.class, args);
	}


}
