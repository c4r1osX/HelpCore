package com.ticketing.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


//- Rutas dinámicas
//- Resolvers para rate limiting
//- Fallbacks para circuit breaker

@Configuration
public class GatewayConfig {
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder){
        return builder.routes()

                // RUTA PARA HEALTH CHECK GLOBAL
                .route("health-check", r -> r
                        .path("/health")
                        .uri("http://localhost:8080")
                        .filters(f -> f
                                .setStatus(HttpStatus.OK)
                                .addResponseHeader("Content-Type", "application/json")
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, body) -> Mono.just("{\"status\":\"UP\",\"service\":\"api-gateway\"}"))
                        )
                )

                // RUTA PARA DOCUMENTACION API
                .route("api-docs", r -> r
                        .path("/api/docs/**")
                        .uri("http://localhost:8080")
                        .filters(f -> f
                                .addResponseHeader("Content-Type", "application/json")
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, body) -> Mono.just(getApiDocumentation()))
                        )
                )

                // RUTA PARA METRICAS
                .route("metrics", r -> r
                        .path("/api/metrics")
                        .and()
                        .header("Authorization")
                        .uri("http://localhost:8080/actuator/metrics")
                        .filters(f -> f
                                .addResponseHeader("X-Metrics-Source", "api-gateway")
                        )
                )

                // FALLBACK - MANEJO DE ERRORES
                .route("fallback-auth", r -> r
                        .path("/fallback/auth")
                        .uri("http://localhost:8080")
                        .filters(f -> f
                                .setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                .addResponseheader("Content-Type","application/json")
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, body) -> Mono.just(
                                                "{\"error\":\"Auth Service Unavailable\",\"message\":\"Please try again later\",\"code\":503}"
                                        ))
                        )
                )

                .route("fallback-users", r -> r
                        .path("/fallback/users")
                        .uri("http://localhost:8080")
                        .filters(f -> f
                                .setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                .addResponseHeader("Content-Type", "application/json")
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, body) -> Mono.just(
                                                "{\"error\":\"User Service Unavailable\",\"message\":\"User operations temporarily unavailable\",\"code\":503}"
                                        ))
                        )
                )

                .route("fallback-tickets", r -> r
                        .path("/fallback/tickets")
                        .uri("http://localhost:8080")
                        .filters(f -> f
                                .setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                .addResponseHeader("Content-Type", "application/json")
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, body) -> Mono.just(
                                                "{\"error\":\"Ticket Service Unavailable\",\"message\":\"Ticket operations temporarily unavailable\",\"code\":503}"
                                        ))
                        )
                )

                .route("fallback-notifications", r -> r
                        .path("/fallback/notifications")
                        .uri("http://localhost:8080")
                        .filters(f -> f
                                .setStatus(HttpStatus.SERVICE_UNAVAILABLE)
                                .addResponseHeader("Content-Type", "application/json")
                                .modifyResponseBody(String.class, String.class,
                                        (exchange, body) -> Mono.just(
                                                "{\"error\":\"Notification Service Unavailable\",\"message\":\"Notifications temporarily unavailable\",\"code\":503}"
                                        ))
                        )
                )
                .build();
    }

    // Key Resolver (Identificar usuario por token y devolver ID o IP) para aplicar Rate Limiting
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                try{
                    // Extraer userId del token
                    String token = authHeader.substring(7);
                    String userId = extractUserIdFromToken(token);

                    if (userId != null) {
                        return Mono.just(userId); // Rate limiting por usuario
                    }
                }catch (Exception e){
                    System.err.println("Error extracting user ID from token: " + e.getMessage());
                }
            }
                // Fallback, usar IP del cliente para rate limiting
                String clientIp = getClientIp(exchange);
                return Mono.just(clientIp);
        };
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

    private String extractUserIdFromToken(String token) {
        try{
            return "default-user";
        }catch (Exception e){
            return null;
        }
    }
}
