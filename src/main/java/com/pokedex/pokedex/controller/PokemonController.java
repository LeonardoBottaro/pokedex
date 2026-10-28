
package com.pokedex.pokedex.controller;

import com.pokedex.pokedex.model.Pokemon;
import com.pokedex.pokedex.repository.PokemonRepository;
import com.pokedex.pokedex.service.PokemonImportService;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {

    private final PokemonRepository pokemonRepository;
    private final PokemonImportService pokemonImportService;

    public PokemonController(
            PokemonRepository pokemonRepository,
            PokemonImportService pokemonImportService) {
        this.pokemonRepository = pokemonRepository;
        this.pokemonImportService = pokemonImportService;
    }

    // Lista todos os Pokémon em ordem alfabética.
    @GetMapping
    public List<Pokemon> listarTodos() {
        return pokemonRepository.findAll(
                Sort.by(Sort.Direction.ASC, "name"));
    }

    // Busca um Pokémon pelo nome completo.
    @GetMapping("/{name}")
    public ResponseEntity<Pokemon> buscarPorNome(
            @PathVariable String name) {
        return pokemonRepository.findByNameIgnoreCase(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Pesquisa Pokémon por parte do nome.
    @GetMapping("/buscar")
    public List<Pokemon> buscarPorParteDoNome(
            @RequestParam String nome) {
        return pokemonRepository
                .findByNameContainingIgnoreCaseOrderByNameAsc(nome);
    }

    // Mantém a rota de importação existente.
    @PostMapping("/importar")
    public String importar() throws IOException {
        return pokemonImportService.importarPokemon();
    }
}
