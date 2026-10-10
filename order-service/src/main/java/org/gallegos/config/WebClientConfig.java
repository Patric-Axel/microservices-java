package org.gallegos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class WebClientConfig {

    private static final String TRACE_ID = "X-Trace-Id";

    @Bean
    WebClient webClient(
            @Value("${inventory.service.url}") String inventoryUrl) {

        return WebClient.builder()
                .baseUrl(inventoryUrl)
                .filter((request, next) -> {

                    ServletRequestAttributes attributes =
                            (ServletRequestAttributes)
                            RequestContextHolder.getRequestAttributes();

                    if (attributes == null) {
                        return next.exchange(request);
                    }

                    HttpServletRequest currentRequest =
                            attributes.getRequest();

                    String traceId =
                            currentRequest.getHeader(TRACE_ID);

                    if (traceId == null || traceId.isBlank()) {
                        return next.exchange(request);
                    }

                    return next.exchange(
                            ClientRequest.from(request)
                                    .header(TRACE_ID, traceId)
                                    .build()
                    );
                })
                .build();
    }
}