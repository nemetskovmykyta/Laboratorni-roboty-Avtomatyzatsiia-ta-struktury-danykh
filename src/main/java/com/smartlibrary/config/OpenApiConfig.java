package com.smartlibrary.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI smartLibraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Smart Library REST API")
                        .description("REST API для автоматизації роботи бібліотеки Smart Library (Лабораторна робота №2)")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Smart Library Support")));
    }
}
