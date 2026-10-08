package com.dental.oms.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    RestClient catalogRestClient(RestClient.Builder builder, DownstreamProperties props) {
        return build(builder, props, props.catalogUrl());
    }

    @Bean
    RestClient inventoryRestClient(RestClient.Builder builder, DownstreamProperties props) {
        return build(builder, props, props.inventoryUrl());
    }

    @Bean
    RestClient pricingRestClient(RestClient.Builder builder, DownstreamProperties props) {
        return build(builder, props, props.pricingUrl());
    }

    /** Uses the Boot-managed builder so Micrometer observation (metrics + trace propagation) stays wired in. */
    private RestClient build(RestClient.Builder builder, DownstreamProperties props, String baseUrl) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeout());
        factory.setReadTimeout(props.readTimeout());
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .requestInterceptor(new HeaderPropagationInterceptor(props.propagateHeaders()))
                .build();
    }
}
