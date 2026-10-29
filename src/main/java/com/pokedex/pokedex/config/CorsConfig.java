
package com.pokedex.pokedex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Indica ao Spring Boot que esta classe contém configurações da aplicação.
@Configuration
public class CorsConfig {

    // Registra as regras de CORS para as rotas da API.
    @Bean
    public WebMvcConfigurer configurarCors() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {

                // Autoriza o frontend executado pelo Vite.
                registry.addMapping("/api/**")
                    .allowedOrigins(
                        "http://localhost:5173",
                        "http://127.0.0.1:5173"
                    )
                    .allowedMethods(
                        "GET", "POST", "PUT", "DELETE", "OPTIONS"
                    )
                    .allowedHeaders("*");
            }
        };
    }
}
