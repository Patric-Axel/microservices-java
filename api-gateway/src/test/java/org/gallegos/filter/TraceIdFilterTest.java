package org.gallegos.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

class TraceIdFilterTest {
	
	// GW-04 / GW-05 - Generar o conservar X-Trace-Id
    //
    // DADO:
    // Una petición puede llegar:
    // - sin X-Trace-Id
    // - con X-Trace-Id vacío
    // - con X-Trace-Id en blanco
    // - con un X-Trace-Id enviado por el cliente
    //
    // CUANDO:
    // La petición atraviesa TraceIdFilter.
    //
    // ENTONCES:
    // - Si no existe o está vacío/blanco, Gateway genera un UUID.
    // - Si existe, Gateway conserva el X-Trace-Id recibido.
    // - El mismo X-Trace-Id se propaga hacia el downstream.
    // - El mismo X-Trace-Id se agrega a la respuesta.
	
	
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "trace-del-cliente"})
    void propagatesReceivedTraceOrGeneratesOneWhenAbsentOrBlank(String incoming) {
        MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get("/api/orders");
        if (incoming != null) {
            request.header("X-Trace-Id", incoming);
        }
        MockServerWebExchange exchange = MockServerWebExchange.from(request.build());
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        new TraceIdFilter().filter(exchange, chain).block(Duration.ofSeconds(5));

        ArgumentCaptor<ServerWebExchange> captured = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(captured.capture());
        String propagated = captured.getValue().getRequest().getHeaders().getFirst("X-Trace-Id");
        assertNotNull(propagated);
        if (incoming == null || incoming.isBlank()) {
            assertEquals(propagated, UUID.fromString(propagated).toString());
        } else {
            assertEquals(incoming, propagated);
        }
        assertEquals(propagated, exchange.getResponse().getHeaders().getFirst("X-Trace-Id"));
    }
}
