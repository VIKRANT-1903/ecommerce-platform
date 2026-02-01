package com.example.ecomm1.offer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class InventoryClientConfig {

    @Bean
    public RestClient inventoryRestClient(
            RestClient.Builder builder,
            @Value("${external.inventory.service.url}") String baseUrl
    ) {
        return builder.baseUrl(baseUrl).build();
    }
}

