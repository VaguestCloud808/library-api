package com.davi.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library API")
                        .version("1.0.0")
                        .description("API REST de Gerenciamento de Biblioteca. " +
                                "Gerencia livros, usuários, empréstimos e multas.")
                        .contact(new Contact()
                                .name("Davi Alves Couto")
                                .email("davialvescouto18@gmail.com")
                                .url("https://github.com/VaguestCloud808"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
