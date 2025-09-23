package com.helpcore.gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;


// * - Capturar todas las excepciones no manejadas
// * - Formatear respuestas de error consistentes
// * - Loggear errores para monitoreo
// * - Evitar exposición de información sensible
// * - Manejar timeouts y errores de circuit breaker
@Component
@Order(-1)  // Alta prioridad
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        ErrorInfo errorInfo = determineErrorInfo(ex);

        response.setStatusCode(errorInfo.status);
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        response.getHeaders().add("X-Error-Source", "api-gateway");
        response.getHeaders().add("X-Error-Timestamp", Instant.now().toString());

        logError(exchange, ex, errorInfo);

        String errorResponse = createErrorResponse(exchange, errorInfo, ex);
        DataBuffer buffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    private ErrorInfo determineErrorInfo(Throwable ex) {
        // RESPONSE STATUS EXCEPTIONS
        if (ex instanceof ResponseStatusException rse) {
            return new ErrorInfo(
                    rse.getStatusCode(),
                    mapStatusToErrorType(rse.getStatusCode()),
                    rse.getReason() != null ? rse.getReason() : "Request failed"
            );
        }

        // CONNECTION TIMEOUT
        if (ex instanceof java.util.concurrent.TimeoutException ||
                ex.getCause() instanceof java.util.concurrent.TimeoutException) {
            return new ErrorInfo(
                    HttpStatus.GATEWAY_TIMEOUT,
                    "GATEWAY_TIMEOUT",
                    "Service timeout - please try again later"
            );
        }

        // CONNECTION REFUSED (Service Down)
        if (ex instanceof java.net.ConnectException ||
                (ex.getCause() instanceof java.net.ConnectException)) {
            return new ErrorInfo(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "SERVICE_UNAVAILABLE",
                    "Service temporarily unavailable"
            );
        }

        // CIRCUIT BREAKER OPEN
        if (ex.getMessage() != null && ex.getMessage().contains("CircuitBreaker")) {
            return new ErrorInfo(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "CIRCUIT_BREAKER_OPEN",
                    "Service is temporarily unavailable due to high error rate"
            );
        }

        // JWT RELATED ERRORS
        if (ex instanceof io.jsonwebtoken.JwtException) {
            return new ErrorInfo(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN",
                    "Invalid or expired authentication token"
            );
        }

        //  RATE LIMITING
        if (ex.getMessage() != null && ex.getMessage().contains("rate limit")) {
            return new ErrorInfo(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "RATE_LIMIT_EXCEEDED",
                    "Rate limit exceeded - please try again later"
            );
        }

        // SSL/TLS ERRORS
        if (ex instanceof javax.net.ssl.SSLException) {
            return new ErrorInfo(
                    HttpStatus.BAD_GATEWAY,
                    "SSL_ERROR",
                    "Secure connection error"
            );
        }

        // GENERAL NETWORK ERRORS
        if (ex instanceof java.io.IOException) {
            return new ErrorInfo(
                    HttpStatus.BAD_GATEWAY,
                    "NETWORK_ERROR",
                    "Network communication error"
            );
        }

        // FALLBACK: INTERNAL SERVER ERROR
        return new ErrorInfo(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "An unexpected error occurred"
        );
    }

    private String mapStatusToErrorType(Object statusCode) {
        int code;

        // Manejar tanto HttpStatus como HttpStatusCode
        if (statusCode instanceof HttpStatus httpStatus) {
            code = httpStatus.value();
        } else if (statusCode instanceof HttpStatusCode httpStatusCode) {
            code = httpStatusCode.value();
        } else {
            return "UNKNOWN_ERROR";
        }

        return switch (code) {
            case 400 -> "BAD_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 409 -> "CONFLICT";
            case 422 -> "UNPROCESSABLE_ENTITY";
            case 429 -> "RATE_LIMIT_EXCEEDED";
            case 500 -> "INTERNAL_ERROR";
            case 502 -> "BAD_GATEWAY";
            case 503 -> "SERVICE_UNAVAILABLE";
            case 504 -> "GATEWAY_TIMEOUT";
            default -> "UNKNOWN_ERROR";
        };
    }

    private String createErrorResponse(ServerWebExchange exchange, ErrorInfo errorInfo, Throwable ex) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        boolean includeDetails = shouldIncludeErrorDetails();

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        jsonBuilder.append("  \"error\": \"").append(errorInfo.type).append("\",\n");
        jsonBuilder.append("  \"message\": \"").append(errorInfo.message).append("\",\n");
        jsonBuilder.append("  \"code\": ").append(errorInfo.status.value()).append(",\n");
        jsonBuilder.append("  \"timestamp\": \"").append(Instant.now().toString()).append("\",\n");
        jsonBuilder.append("  \"path\": \"").append(path).append("\",\n");
        jsonBuilder.append("  \"method\": \"").append(method).append("\"");

        if (includeDetails && ex != null) {
            jsonBuilder.append(",\n  \"details\": {\n");
            jsonBuilder.append("    \"exception\": \"").append(ex.getClass().getSimpleName()).append("\",\n");

            if (ex.getMessage() != null) {
                String sanitizedMessage = ex.getMessage().replace("\"", "\\\"").replace("\n", "\\n");
                jsonBuilder.append("    \"exceptionMessage\": \"").append(sanitizedMessage).append("\",\n");
            }

            if (isDevelopmentEnvironment()) {
                jsonBuilder.append("    \"stackTrace\": \"").append(getStackTraceAsString(ex)).append("\"\n");
            } else {
                jsonBuilder.append("    \"trace\": \"Contact support with timestamp for assistance\"\n");
            }

            jsonBuilder.append("  }");
        }

        jsonBuilder.append(",\n  \"help\": {\n");
        jsonBuilder.append("    \"documentation\": \"/api/docs\",\n");
        jsonBuilder.append("    \"support\": \"support@helpcore-system.com\",\n");
        jsonBuilder.append("    \"status\": \"/health\"\n");
        jsonBuilder.append("  }\n");

        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }

    private void logError(ServerWebExchange exchange, Throwable ex, ErrorInfo errorInfo) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        String clientIp = getClientIp(exchange);

        if (errorInfo.status.is5xxServerError()) {
            System.err.printf("[ERROR] %s %s - %s (IP: %s) - %s: %s%n",
                    method, path, errorInfo.status, clientIp,
                    ex.getClass().getSimpleName(), ex.getMessage());

            ex.printStackTrace();

        } else if (errorInfo.status.is4xxClientError() && errorInfo.status != HttpStatus.NOT_FOUND) {
            System.out.printf("[WARN] %s %s - %s (IP: %s) - %s%n",
                    method, path, errorInfo.status, clientIp, errorInfo.message);

        } else {
            System.out.printf("[INFO] %s %s - %s (IP: %s)%n",
                    method, path, errorInfo.status, clientIp);
        }
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }

    private String getStackTraceAsString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append(element.toString()).append("\\n");
            if (sb.length() > 1000) break;
        }
        return sb.toString().replace("\"", "\\\"");
    }

    private boolean shouldIncludeErrorDetails() {
        return isDevelopmentEnvironment();
    }

    private boolean isDevelopmentEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        return "dev".equals(profile) || "development".equals(profile);
    }

    private static class ErrorInfo {
        final HttpStatusCode status;
        final String type;
        final String message;

        ErrorInfo(HttpStatusCode status, String type, String message) {
            this.status = status;
            this.type = type;
            this.message = message;
        }
    }
}