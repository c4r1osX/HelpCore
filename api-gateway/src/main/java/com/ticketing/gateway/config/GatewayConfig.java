package com.ticketing.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

//- Rutas dinámicas
//- Resolvers para rate limiting
//- Fallbacks para circuit breaker

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()

                // Ruta para documentación
                .route("docs", r -> r
                        .path("/api/docs/**")
                        .uri("http://localhost:8080"))

                // Ruta para health check
                .route("health-check", r -> r
                        .path("/health")
                        .uri("http://localhost:8080"))

                // Ruta para fallback de auth
                .route("auth-fallback", r -> r
                        .path("/fallback/auth")
                        .uri("http://localhost:8080"))

                // Ruta para auth-service vía Eureka
                .route("auth-service", r -> r
                        .path("/api/auth/**")
                        .filters(f -> f
                                .rewritePath("/api/auth/(?<segment>.*)", "/auth/${segment}")
                                .addRequestHeader("X-Gateway-Debug", "true")
                                .addResponseHeader("X-Gateway-Route", "auth-service")
                        )
                        .uri("lb://auth-service"))

                .route("ticket-service", r -> r
                        .path("/api/{segment}/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<prefix>ticket|categoria-ticket)(?<remaining>/?.*)", "/${prefix}${remaining}")
                                .addRequestHeader("X-Gateway-Debug", "true")
                                .addResponseHeader("X-Gateway-Route", "ticket-service")
                        )
                        .uri("lb://ticket-service"))
                .build();
    }

    // Key Resolver (Identificar IP del cliente)
    // Alternativa para request sin autenticacion
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(getClientIp(exchange));
    }

    private String getClientIp(ServerWebExchange exchange) {
        // ######## PRIORIDAD 1: ########
        // ServerWebExchange: Interfaz de Spring WebFlux, representa el contexto completo de una solicitud HTTP en el servidor.
        // X-Forwarded-For: Header HTTP estandar para identificar la IP original del cliente cuando hace una peticion.
        // El header puede contener múltiples IPs (la primera es la ip real del cliente, y las demas proxies intermedios)
        // Formato: "client_ip, proxy1_ip, proxy2_ip, proxy3_ip"
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
            // split(",") → ["203.0.113.1", " 198.51.100.2", " 192.0.2.1"]
            // [0] → "203.0.113.1" (primera IP = cliente real)
            // trim() → remueve espacios en blanco por si acaso
        }

        // ######## PRIORIDAD 2: ########
        // X-Real-IP: Header HTTP que transmite la IP real del cliente (No proxys)
        // Formato: "client_ip"
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp; // Devolver directamente la ip
        }

        // Fallback a la IP remota del socket
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    // Documentacion basica de la API
    private String getApiDocumentation() {
        return """
            {
                "title": "Ticketing System API Gateway",
                "version": "1.0.0",
                "description": "API Gateway para sistema de gestión de tickets",
                "current_phase": "Authentication Only",
                "available_endpoints": {
                    "auth": {
                        "base": "/api/auth",
                        "methods": [
                            "POST /api/auth/login - Iniciar sesión",
                            "POST /api/auth/register - Registrar usuario",
                            "POST /api/auth/refresh - Renovar token"
                        ],
                        "description": "Endpoints de autenticación que se redirigen al Auth Service en puerto 8081"
                    }
                },
                "system_endpoints": {
                    "health": "GET /health - Estado del API Gateway",
                    "docs": "GET /api/docs - Esta documentación"
                },
                "rate_limits": {
                    "auth_endpoints": "10 requests/second por IP",
                    "burst_capacity": "20 requests máximo en ráfagas"
                },
                "security": {
                    "cors": "Habilitado para http://localhost:4200 (Angular)",
                    "rate_limiting": "Basado en IP del cliente",
                    "jwt_validation": "Implementado para autenticacion de microservicios"
                },
                "backend_services": {
                    "auth_service": {
                        "url": "http://localhost:8081",
                        "status": "active",
                        "endpoints": "/auth/*"
                    }
                },
                "next_phase": "Implementar User Service y Ticket Service"
            }
            """;
    }
}
