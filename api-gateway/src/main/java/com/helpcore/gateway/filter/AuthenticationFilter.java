package com.helpcore.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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

// * - Validar JWT tokens en headers Authorization
// * - Extraer información del usuario del token
// * - Establecer contexto de seguridad para requests autenticados
// * - Rechazar requests con tokens inválidos o expirados
// * - Pasar información del usuario a microservicios downstream

@Component
public class AuthenticationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

//     * 1. Verificar si el endpoint requiere autenticación
//     * 2. Extraer JWT del header Authorization
//     * 3. Validar el token (firma, expiración, estructura)
//     * 4. Extraer claims del usuario
//     * 5. Establecer contexto de seguridad
//     * 6. Pasar información a microservicios
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // SKIP AUTENTICACIÓN PARA RUTAS PÚBLICAS
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // EXTRAER JWT TOKEN
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);

        try {
            // VALIDAR Y DECODIFICAR JWT
            Claims claims = validateAndParseToken(token);

            // EXTRAER INFORMACIÓN DEL USUARIO
            String userId = claims.getId();
            String username = claims.getSubject();
            String usuarioName = claims.get("usuario", String.class);

            // CREAR AUTHENTICATION OBJECT
            List<SimpleGrantedAuthority> grantedAuthorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER")
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);

            // AGREGAR HEADERS PARA MICROSERVICIOS
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId != null ? userId : username)    // ID o username
                    .header("X-User-Username", username)                        // Username para logs
                    .header("X-User-Role", "USER")                 // Rol por defecto
                    .header("X-Auth-Source", "api-gateway")        // Identificar source de auth
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            // ESTABLECER CONTEXTO DE SEGURIDAD
            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            return handleUnauthorized(exchange, "Invalid JWT token: " + e.getMessage());
        }
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

    // Extraer el user ID del JWT token
    private String extractUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getId();
            return userId != null ? userId : claims.getSubject();

        } catch (Exception e) {
            return null;
        }
    }

    // Verificar si una ruta es pública
    private boolean isPublicPath(String path) {
        // Rutas de autenticación
        if (path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register")) {
            return true;
        }

        // Health checks y documentación
        if (path.equals("/health") ||
            path.startsWith("/api/docs") ||
            path.equals("/actuator/health")) {
            return true;
        }

        // Fallback endpoints
        if (path.startsWith("/fallback/")) {
            return true;
        }

        return false;
    }


    // Manejar requests no autorizados
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("""
            {
                "error": "Unauthorized",
                "message": "%s",
                "code": 401,
                "timestamp": "%s",
                "path": "%s"
            }
            """,
                message,
                java.time.Instant.now().toString(),
                exchange.getRequest().getURI().getPath()
        );

        org.springframework.core.io.buffer.DataBuffer buffer =
                response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }
}