package org.gallegos.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.WebFilterChainProxy;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import reactor.core.publisher.Mono;

// Component tests of the production security chain, without a server or Keycloak.
class SecurityConfigTest {
	private ReactiveJwtDecoder decoder;
	private WebFilterChainProxy security;

	@BeforeEach
	void setUp() {

	    decoder = mock(ReactiveJwtDecoder.class);

	    ServerHttpSecurity http = ServerHttpSecurity.http();

	    http.oauth2ResourceServer(oauth ->
	        oauth.jwt(jwt -> jwt.jwtDecoder(decoder))
	    );

	    ObjectMapper objectMapper = new ObjectMapper();
	    objectMapper.registerModule(new JavaTimeModule());

	    CustomAuthenticationEntryPoint entryPoint =
	            new CustomAuthenticationEntryPoint(objectMapper);

	    security = new WebFilterChainProxy(
	            new SecurityConfig(entryPoint)
	                    .securityWebFilterChain(http)
	    );
	}

	// GW-01 - Rechazar petición sin JWT o con JWT inválido
	// DADO: una petición a /api/orders sin token o con un token
	// que el ReactiveJwtDecoder considera inválido.
	// CUANDO: la petición atraviesa la cadena de seguridad.
	// ENTONCES: Gateway responde 401 UNAUTHORIZED y la petición
	// no llega al servicio downstream.

	@ParameterizedTest
	@ValueSource(booleans = { false, true })
	void rejectsMissingOrInvalidJwtWithoutReachingDownstream(boolean sendInvalidToken) {
		MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get("/api/orders");
		if (sendInvalidToken) {
			request.header("Authorization", "Bearer invalid-token");
			when(decoder.decode("invalid-token")).thenReturn(Mono.error(new BadJwtException("Rejected token")));
		}
		MockServerWebExchange exchange = MockServerWebExchange.from(request.build());
		AtomicBoolean downstreamCalled = new AtomicBoolean();

		security.filter(exchange, next -> {
			downstreamCalled.set(true);
			return Mono.empty();
		}).block(Duration.ofSeconds(5));

		assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
		assertFalse(downstreamCalled.get());
		if (sendInvalidToken) {
			verify(decoder).decode("invalid-token");
		} else {
			verifyNoInteractions(decoder);
		}
	}

	// GW-02 - Permitir petición con JWT válido
	// DADO: una petición contiene un Bearer Token que el
	// ReactiveJwtDecoder acepta como JWT válido.
	// CUANDO: la petición atraviesa la cadena de seguridad.
	// ENTONCES: Gateway permite continuar la petición hacia
	// el siguiente filtro/downstream.
	@Test
	void acceptedJwtAllowsRequestToReachDownstream() {
		Jwt jwt = Jwt.withTokenValue("accepted-token").header("alg", "RS256").subject("cliente").issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(300)).build();
		when(decoder.decode("accepted-token")).thenReturn(Mono.just(jwt));
		MockServerWebExchange exchange = MockServerWebExchange.from(
				MockServerHttpRequest.get("/api/orders").header("Authorization", "Bearer accepted-token").build());
		AtomicBoolean downstreamCalled = new AtomicBoolean();

		security.filter(exchange, next -> {
			downstreamCalled.set(true);
			next.getResponse().setStatusCode(HttpStatus.OK);
			return next.getResponse().setComplete();
		}).block(Duration.ofSeconds(5));

		assertTrue(downstreamCalled.get());
		assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
		verify(decoder).decode("accepted-token");
	}
}
