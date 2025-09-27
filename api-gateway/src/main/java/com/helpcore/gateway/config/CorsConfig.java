//package com.helpcore.gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//import org.springframework.web.server.ServerWebExchange;
//import org.springframework.web.server.WebFilter;
//import org.springframework.web.server.WebFilterChain;
//import reactor.core.publisher.Mono;
//
//import java.util.Arrays;
//
//// * Configuración CORS
//// * (http://localhost:4200) -> (http://localhost:8080)
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//
//        corsConfig.setAllowedOriginPatterns(Arrays.asList(
//                "http://localhost:4200",
//                "http://127.0.0.1:4200",
//                "https://helpcore-app.com"
//        ));
//
//        // ===== MÉTODOS HTTP PERMITIDOS =====
//        corsConfig.setAllowedMethods(Arrays.asList(
//                HttpMethod.GET.name(),
//                HttpMethod.POST.name(),
//                HttpMethod.PUT.name(),
//                HttpMethod.PATCH.name(),
//                HttpMethod.DELETE.name(),
//                HttpMethod.OPTIONS.name()
//        ));
//
//        // ===== HEADERS PERMITIDOS =====
//        corsConfig.setAllowedHeaders(Arrays.asList(
//                HttpHeaders.AUTHORIZATION,   // JWT Bearer tokens
//                HttpHeaders.CONTENT_TYPE,    // application/json, etc.
//                HttpHeaders.ACCEPT,          // Tipos de respuesta aceptados
//                "X-Requested-With",          // Headers de AJAX
//                "X-User-Role",               // Header personalizado para roles
//                "X-Client-Version",          // Versión del cliente Angular
//                "X-Request-ID"               // ID de tracking de requests
//        ));
//
//        // ===== HEADERS EXPUESTOS =====
//        corsConfig.setExposedHeaders(Arrays.asList(
//                "X-Total-Count",             // Total de registros en listados
//                "X-Page-Count",              // Total de páginas
//                "X-Current-Page",            // Página actual
//                "X-Rate-Limit-Remaining",    // Rate limit restante
//                "X-Response-Time",           // Tiempo de respuesta
//                "X-API-Gateway"              // Identificador del gateway
//        ));
//
//        corsConfig.setAllowCredentials(true);    // Permitir cookies y JWT tokens
//        corsConfig.setMaxAge(3600L);             // Cache por 1 hora
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfig);
//
//        return new CorsWebFilter(source);
//    }
//
//    @Bean
//    public WebFilter corsPreflightFilter() {
//        return (ServerWebExchange exchange, WebFilterChain chain) -> {
//            ServerHttpRequest request = exchange.getRequest();
//
//            if (HttpMethod.OPTIONS.equals(request.getMethod())) {
//                ServerHttpResponse response = exchange.getResponse();
//                HttpHeaders headers = response.getHeaders();
//
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
//                        getOriginFromRequest(request));
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
//                        "GET, POST, PUT, PATCH, DELETE, OPTIONS");
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
//                        "Authorization, Content-Type, Accept, X-Requested-With, X-User-Role, X-Client-Version, X-Request-ID");
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
//                headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
//
//                response.setStatusCode(HttpStatus.OK);
//                return Mono.empty();
//            }
//
//            return chain.filter(exchange);
//        };
//    }
//
//    private String getOriginFromRequest(ServerHttpRequest request) {
//        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
//
//        if (origin != null && isAllowedOrigin(origin)) {
//            return origin;
//        }
//
//        return "http://localhost:4200";
//    }
//
//    private boolean isAllowedOrigin(String origin) {
//        return origin.equals("http://localhost:4200") ||
//               origin.equals("http://127.0.0.1:4200") ||
//               origin.equals("https://helpcore-app.com");
//    }
//}