package com.dental.oms.order.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Copies selected inbound headers onto outbound calls.
 *
 * Why this matters for Istio: the mesh routes on headers it can see. If the ingress receives
 * "x-pricing-version: v2" but order-service does not forward it to pricing-service, header-based
 * routing silently stops at the first hop. Same for x-request-id (access-log correlation).
 * Trace headers (traceparent / b3) are already propagated by Micrometer Tracing.
 */
public class HeaderPropagationInterceptor implements ClientHttpRequestInterceptor {

    private final List<String> headers;

    public HeaderPropagationInterceptor(List<String> headers) {
        this.headers = headers;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest inbound = attrs.getRequest();
            for (String name : headers) {
                String value = inbound.getHeader(name);
                if (value != null && !request.getHeaders().containsKey(name)) {
                    request.getHeaders().add(name, value);
                }
            }
        }
        return execution.execute(request, body);
    }
}
