package com.pokedex.pokedex.service;

// Importa a entidade que representa os Pokémon no banco.
import com.pokedex.pokedex.model.Pokemon;

// Importa o repositório responsável por acessar a tabela pokemon.
import com.pokedex.pokedex.repository.PokemonRepository;

// Permite converter o conteúdo do JSON em objetos Java.
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// Permite identificar a classe como um serviço do Spring.
import org.springframework.stereotype.Service;

// Permite executar a importação dentro de uma transação.
import org.springframework.transaction.annotation.Transactional;

// Importa classes para trabalhar com arquivos.
import java.io.IOException;
import java.io.InputStream;

// Importa estruturas para armazenar os Pokémon lidos.
import java.util.List;
import java.util.Optional;

// Indica ao Spring que esta classe contém regras de negócio.
@Service
public class PokemonImportService {

    // Caminho do arquivo JSON localizado na raiz do projeto.
    private static final String CAMINHO_JSON = "pokemon.json";

    // Repositório usado para consultar e salvar os Pokémon.
    private final PokemonRepository pokemonRepository;

    // Conversor usado para transformar JSON em objetos Java.
    private final ObjectMapper objectMapper;

    // O Spring fornece automaticamente as dependências necessárias.
    public PokemonImportService(
            PokemonRepository pokemonRepository,
            ObjectMapper objectMapper) {

        // Guarda o repositório recebido.
        this.pokemonRepository = pokemonRepository;

        // Guarda o conversor JSON recebido.
        this.objectMapper = objectMapper;
    }

    // Executa toda a importação em uma transação.
    @Transactional
    public String importarPokemon() throws IOException {

        // Abre o arquivo JSON a partir da pasta de execução da aplicação.
        try (InputStream arquivo = java.nio.file.Files.newInputStream(
                java.nio.file.Path.of(CAMINHO_JSON))) {

            // Converte o conteúdo do arquivo em uma lista de Pokémon.
            List<Pokemon> pokemons = objectMapper.readValue(
                    arquivo,
                    new TypeReference<List<Pokemon>>() {
                    }
            );

            // Conta quantos Pokémon foram inseridos ou atualizados.
            int processados = 0;

            // Percorre todos os Pokémon encontrados no JSON.
            for (Pokemon pokemon : pokemons) {

                // Procura um registro existente usando o nome do Pokémon.
                Optional<Pokemon> existente =
                        pokemonRepository.findByName(pokemon.getName());

                // Se já existir, reaproveita seu identificador.
                if (existente.isPresent()) {
                    pokemon.setId(existente.get().getId());
                }

                // Salva o Pokémon novo ou atualiza o que já existe.
                pokemonRepository.save(pokemon);

                // Atualiza a quantidade de registros processados.
                processados++;
            }

            // Retorna um resumo da importação.
            return "Importação concluída. Pokémon processados: "
                    + processados;
        }
    }
}
