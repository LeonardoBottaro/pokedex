
package com.pokedex.pokedex.controller;

import com.pokedex.pokedex.dto.PokemonLevelUpMoveResponse;
import com.pokedex.pokedex.model.PokemonLevelUpMove;
import com.pokedex.pokedex.repository.PokemonLevelUpMoveRepository;
import com.pokedex.pokedex.service.PokemonLevelUpMoveImportService;
import com.pokedex.pokedex.service.PokemonLevelUpMoveImportService.ImportReport;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/level-up-moves")
public class PokemonLevelUpMoveController {

    private final PokemonLevelUpMoveImportService importService;
    private final PokemonLevelUpMoveRepository repository;

    public PokemonLevelUpMoveController(
            PokemonLevelUpMoveImportService importService,
            PokemonLevelUpMoveRepository repository) {
        this.importService = importService;
        this.repository = repository;
    }

    // Lista os movimentos de um Pokémon em ordem de nível.
    @GetMapping("/{pokemonName}")
    public ResponseEntity<List<PokemonLevelUpMoveResponse>> listarPorPokemon(
            @PathVariable String pokemonName) {

        String nomeNormalizado = pokemonName
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase();

        List<PokemonLevelUpMoveResponse> movimentos = repository
                .findByPokemonNameOrderByLevelAsc(nomeNormalizado)
                .stream()
                .map(registro -> new PokemonLevelUpMoveResponse(
                        registro.getLevel(),
                        new PokemonLevelUpMoveResponse.MoveDetails(
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

    @PostMapping(
            value = "/diagnosticar",
            consumes = "text/plain",
            produces = "application/json"
    )
    public ResponseEntity<ImportReport> diagnosticar(
            @RequestBody String caminho) throws IOException {

        Path caminhoArquivo = Path.of(caminho.trim());

        ImportReport relatorio =
                importService.diagnosticarMovimentosPorNivel(caminhoArquivo);

        return ResponseEntity.ok(relatorio);
    }

    @PostMapping(
            value = "/import",
            consumes = "text/plain",
            produces = "application/json"
    )
    public ResponseEntity<ImportReport> importar(
            @RequestBody String caminho) throws IOException {

        Path caminhoArquivo = Path.of(caminho.trim());

        ImportReport relatorio =
                importService.importarMovimentosPorNivel(caminhoArquivo);

        return ResponseEntity.ok(relatorio);
    }
}
