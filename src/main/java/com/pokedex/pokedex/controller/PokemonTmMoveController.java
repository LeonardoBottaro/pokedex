
package com.pokedex.pokedex.controller;

import com.pokedex.pokedex.dto.PokemonTmMoveResponse;
import com.pokedex.pokedex.model.PokemonTmMove;
import com.pokedex.pokedex.repository.PokemonTmMoveRepository;
import com.pokedex.pokedex.service.PokemonTmMoveImportService;
import com.pokedex.pokedex.service.PokemonTmMoveImportService.ImportReport;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/tm-moves")
public class PokemonTmMoveController {

    private final PokemonTmMoveImportService importService;
    private final PokemonTmMoveRepository repository;

    public PokemonTmMoveController(
            PokemonTmMoveImportService importService,
            PokemonTmMoveRepository repository) {
        this.importService = importService;
        this.repository = repository;
    }

    // Lista os movimentos TM/HM de um Pokémon.
    @GetMapping("/{pokemonName}")
    public ResponseEntity<List<PokemonTmMoveResponse>> listarPorPokemon(
            @PathVariable String pokemonName) {

        List<PokemonTmMoveResponse> movimentos = repository
                .findByPokemon_NameIgnoreCaseOrderByMove_NameAsc(pokemonName)
                .stream()
                .map(registro -> new PokemonTmMoveResponse(
                        new PokemonTmMoveResponse.MoveDetails(
                                registro.getMove().getName(),
                                registro.getMove().getType(),
                                registro.getMove().getPower(),
                                registro.getMove().getAccuracy(),
                                registro.getMove().getPp(),
                                registro.getMove().getCategory(),
                                registro.getMove().getEffect(),
                                registro.getMove().getEffectChance()
                        )
                ))
                .toList();

        return ResponseEntity.ok(movimentos);
    }

    @PostMapping(value = "/diagnosticar",
            consumes = "text/plain",
            produces = "application/json")
    public ResponseEntity<ImportReport> diagnosticar(
            @RequestBody String caminho) throws IOException {
        return ResponseEntity.ok(
                importService.diagnosticar(Path.of(caminho.trim())));
    }

    @PostMapping(value = "/import",
            consumes = "text/plain",
            produces = "application/json")
    public ResponseEntity<ImportReport> importar(
            @RequestBody String caminho) throws IOException {
        return ResponseEntity.ok(
                importService.importar(Path.of(caminho.trim())));
    }
}
