package com.helpcore.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

// * CORRECCIONES APLICADAS:
// * 1. Compatibilidad con jjwt 0.11.5 (mismo que auth-service)
// * 2. Return temprano para rutas públicas
// * 3. Validación JWT robusta
// * 4. Logging detallado para debugging
@Component
public class AuthenticationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (request.getMethod() == HttpMethod.OPTIONS) {
            // Permitir preflight sin autenticación
            return chain.filter(exchange);
        }

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // EXTRAER JWT TOKEN DE COOKIES
        String token = getAccessTokenFromCookies(request);

        if (token == null || token.isEmpty()) {
            return handleUnauthorized(exchange, "Missing or invalid access token");
        }

        try {
            Claims claims = validateAndParseToken(token);

            // EXTRAER INFORMACIÓN DEL USUARIO
            String userId = claims.getId();
            String username = claims.getSubject();
            String usuarioName = claims.get("usuario", String.class);

            if (username == null || username.trim().isEmpty()) {
                return handleUnauthorized(exchange, "Invalid token: missing username");
            }

            // CREAR AUTHENTICATION OBJECT
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);

            // AGREGAR HEADERS PARA MICROSERVICIOS DOWNSTREAM
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId : username)
                    .header("X-User-Username", username)
                    .header("X-User-Role", "USER")
                    .header("X-Auth-Source", "api-gateway")
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            // ESTABLECER CONTEXTO DE SEGURIDAD Y CONTINUAR
            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            return handleUnauthorized(exchange, "Invalid JWT token: " + e.getMessage());
        }
    }

    private String getAccessTokenFromCookies(ServerHttpRequest request) {
        return request.getCookies().getFirst("accessToken") != null
                ? request.getCookies().getFirst("accessToken").getValue()
                : null;
    }


    // Validar y parsear el JWT token
    private Claims validateAndParseToken(String token) throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Verificar expiración
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
            throw new Exception("Token has expired");
        }

        // Verificar claims requeridos
        if (claims.getSubject() == null || claims.getSubject().isEmpty()) {
            throw new Exception("Token missing subject (username)");
        }

        // Verificar que tenga el claim 'usuario'
        if (claims.get("usuario") == null) {
            throw new Exception("Token missing usuario claim");
        }

        return claims;
    }

    // Verificar si una ruta es pública
    private boolean isPublicPath(String path) {
        if (path == null) return false;

        // RUTAS DE AUTENTICACIÓN PÚBLICAS
        if (path.equals("/api/auth/login") ||
                path.equals("/api/auth/register")) {
            return true;
        }

        // HEALTH CHECKS Y DOCUMENTACIÓN
        if (path.equals("/health") ||
                path.startsWith("/api/docs") ||
                path.equals("/actuator/health")) {
            return true;
        }

        // ENDPOINTS DE FALLBACK
        if (path.startsWith("/fallback/")) {
            return true;
        }
        return false;
    }

    // MANEJAR REQUESTS NO AUTORIZADOS
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        headers.add("Access-Control-Allow-Origin", "http://localhost:4200");
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type,Authorization");

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        response.getHeaders().add("X-Error-Source", "api-gateway-auth-filter");

        String body = String.format("""
            {
                "error": "Unauthorized",
                "message": "%s",
                "code": 401,
                "timestamp": "%s",
                "path": "%s",
                "method": "%s"
            }
            """,
                message,
                java.time.Instant.now().toString(),
                exchange.getRequest().getURI().getPath(),
                exchange.getRequest().getMethod().name()
        );

        org.springframework.core.io.buffer.DataBuffer buffer =
                response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}