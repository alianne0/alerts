package com.safetynet.alerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entrypoint for our alerts appklication
 */
@SpringBootApplication
public class AlertsApplication {

	/**
	 * Runs the application
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(AlertsApplication.class, args);
	}

}
