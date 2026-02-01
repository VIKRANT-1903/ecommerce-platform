package com.example.searchservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "search.indices")
public class SearchIndexProperties {

    private String product;
    private String offer;
    private String suggest;

}
