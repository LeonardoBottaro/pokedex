package com.pokedex.pokedex.config;

// Importa a classe responsável pela conversão entre JSON e objetos Java.
import com.fasterxml.jackson.databind.ObjectMapper;

// Importa a anotação que registra esta classe como configuração do Spring.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Indica que esta classe contém configurações da aplicação.
@Configuration
public class JacksonConfig {

    // Registra um ObjectMapper como componente gerenciado pelo Spring.
    @Bean
    public ObjectMapper objectMapper() {

        // Cria e retorna o conversor utilizado para ler o arquivo JSON.
        return new ObjectMapper();
    }
}
