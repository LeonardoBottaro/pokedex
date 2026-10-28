package com.pokedex.pokedex.controller;

// Importa a entidade Pokémon.
import com.pokedex.pokedex.model.Pokemon;

// Importa o repositório de Pokémon.
import com.pokedex.pokedex.repository.PokemonRepository;

// Importa o serviço que lê o JSON e grava os dados no banco.
import com.pokedex.pokedex.service.PokemonImportService;

// Permite definir endpoints REST.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa a lista usada na resposta da consulta.
import java.util.List;

// Marca a classe como um controller REST.
@RestController

// Define o prefixo das rotas desta classe.
@RequestMapping("/api/pokemon")
public class PokemonController {

    // Repositório usado para consultar os Pokémon.
    private final PokemonRepository pokemonRepository;

    // Serviço responsável pela importação.
    private final PokemonImportService pokemonImportService;

    // O Spring injeta as dependências necessárias.
    public PokemonController(
            PokemonRepository pokemonRepository,
            PokemonImportService pokemonImportService) {

        // Guarda o repositório.
        this.pokemonRepository = pokemonRepository;

        // Guarda o serviço de importação.
        this.pokemonImportService = pokemonImportService;
    }

    // Lista todos os Pokémon cadastrados.
    @GetMapping
    public List<Pokemon> listarTodos() {

        // Consulta os registros e os retorna como JSON.
        return pokemonRepository.findAll();
    }

    // Executa a importação quando uma requisição POST é recebida.
    @PostMapping("/importar")
    public String importar() throws java.io.IOException {

        // Chama o serviço que lê o JSON e grava os registros.
        return pokemonImportService.importarPokemon();
    }
}
