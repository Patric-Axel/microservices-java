package org.gallegos.filter;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

	private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
	private static final String TRACE_ID = "X-Trace-Id";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		String traceId = exchange.getRequest()
		        .getHeaders()
		        .getFirst(TRACE_ID);

		if (traceId == null || traceId.isBlank()) {
		    traceId = UUID.randomUUID().toString();
		}

		exchange.getResponse()
		        .getHeaders()
		        .set(TRACE_ID, traceId);

		//GENERAMOS TRACE_ID
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
		}

		final String finalTraceId = traceId;

		//AGREGAMOS EL TRACE_ID AL REQUEST
		ServerWebExchange mutatedExchange = exchange.mutate().request(request -> request.headers(headers -> {
			headers.set(TRACE_ID, finalTraceId);
		})).build();

		//AGREGAMOS EL TRACE_ID AL RESPONSE
		mutatedExchange.getResponse().getHeaders().set(TRACE_ID, finalTraceId);
		log.info("X-Trace-Id={} Method={} Path={}", finalTraceId, exchange.getRequest().getMethod(),
				exchange.getRequest().getURI().getPath());
		return chain.filter(mutatedExchange);
	}

	@Override
	public int getOrder() {
		return -1;
	}
}