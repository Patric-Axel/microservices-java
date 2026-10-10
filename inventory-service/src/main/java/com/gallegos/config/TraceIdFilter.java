package com.gallegos.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class TraceIdFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(TraceIdFilter.class);
	private static final String TRACE_ID = "X-Trace-Id";

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, 
			HttpServletResponse response, 
			FilterChain filterChain)
			throws ServletException, IOException {

		String traceId = request.getHeader(TRACE_ID);
		log.info("X-Trace-Id={} Method={} Path={}", 
				traceId,
				request.getMethod(), 
				request.getRequestURI());
		
		filterChain.doFilter(request, response);
	}
}