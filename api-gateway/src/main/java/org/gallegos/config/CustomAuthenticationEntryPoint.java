package org.gallegos.config;

import java.time.LocalDateTime;
import java.util.UUID;

import org.gallegos.dto.ErrorResponse;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint
        implements ServerAuthenticationEntryPoint {

    private static final String TRACE_ID = "X-Trace-Id";
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException ex) {

        String traceId = exchange.getRequest()
                .getHeaders()
                .getFirst(TRACE_ID);

        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        exchange.getResponse()
                .getHeaders()
                .set(TRACE_ID, traceId);

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                "Token requerido o inválido",
                traceId
        );

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] json = objectMapper.writeValueAsBytes(error);

            DataBuffer buffer = exchange.getResponse()
                    .bufferFactory()
                    .wrap(json);

            return exchange.getResponse()
                    .writeWith(Mono.just(buffer));

        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }
}