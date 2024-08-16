package com.uq.jokievents;

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
