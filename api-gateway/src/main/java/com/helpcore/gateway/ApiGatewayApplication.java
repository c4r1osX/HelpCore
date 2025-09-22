package com.helpcore.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// - Punto de entrada único para todas las peticiones del frontend
// - Ruteo hacia microservicios
// - Validación de JWT tokens
// - CORS para Angular
// - Rate limiting y circuit breaker
// - Balanceo de carga automático
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);

		System.out.println("=================================");
		System.out.println("Puerto: 8080");
		System.out.println("Eureka: http://localhost:8761");
		System.out.println("Config Server: http://localhost:8888");
		System.out.println("CORS habilitado para: http://localhost:4200");
		System.out.println("=================================");
	}
}
