package com.duvi.gateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Autowired
    RouteValidator routeValidator;

    @Autowired
    WebClient.Builder webClientBuilder;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                //Check whether if header has auth token or not
                if (!exchange.getRequest().getHeaders().containsKey("Authorization")) {
                    throw new RuntimeException("No auth Kopfer, Bruder!");
                }
                String token = exchange.getRequest().getHeaders().get("Authorization").getFirst();
                try {
                    webClientBuilder
                            .build()
                            .get().uri("api/auth/login").header("Authorization", token);
                } catch (Exception exception) {
                    throw new RuntimeException("Exception while validating token " + exception.getStackTrace());
                }
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {

    }
}
