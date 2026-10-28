package com.pokedex.pokedex.controller;

// Importa o serviço responsável pela importação.
import com.pokedex.pokedex.service.PokemonLevelUpMoveImportService;

// Importa a classe que representa o relatório da importação.
import com.pokedex.pokedex.service.PokemonLevelUpMoveImportService.ImportReport;

// Importa a entidade dos movimentos aprendidos por nível.
import com.pokedex.pokedex.model.PokemonLevelUpMove;

// Importa o repositório de movimentos por nível.
import com.pokedex.pokedex.repository.PokemonLevelUpMoveRepository;

// Importa recursos do Spring para criar endpoints REST.
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Importa recursos para lidar com caminhos de arquivos.
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

// Define este controlador como uma API REST.
@RestController

// Define o caminho base dos endpoints.
@RequestMapping("/api/level-up-moves")
public class PokemonLevelUpMoveController {

    // Serviço e repositório utilizados pelo controlador.
    private final PokemonLevelUpMoveImportService importService;
    private final PokemonLevelUpMoveRepository repository;

    // Recebe as dependências gerenciadas pelo Spring.
    public PokemonLevelUpMoveController(
            PokemonLevelUpMoveImportService importService,
            PokemonLevelUpMoveRepository repository) {
        this.importService = importService;
        this.repository = repository;
    }

    // Retorna os movimentos aprendidos por um Pokémon, ordenados por nível.
    @GetMapping("/{pokemonName}")
    public List<PokemonLevelUpMove> listarPorPokemon(
            @PathVariable String pokemonName) {
        return repository.findByPokemonNameOrderByLevelAsc(pokemonName);
    }

    
    
    // Recebe o caminho do arquivo no corpo da requisição.
    @PostMapping(
            value = "/diagnosticar",
            consumes = "text/plain",
            produces = "application/json"
    )
    public ResponseEntity<ImportReport> diagnosticar(
            @org.springframework.web.bind.annotation.RequestBody String caminho)
            throws IOException {

        // Remove espaços e quebras de linha desnecessários.
        Path caminhoArquivo = Path.of(caminho.trim());

        // Analisa os dados sem gravar registros no banco.
        ImportReport relatorio =
                importService.diagnosticarMovimentosPorNivel(caminhoArquivo);

        // Retorna o relatório em JSON.
        return ResponseEntity.ok(relatorio);
    }

    // Recebe o caminho do arquivo no corpo da requisição.
    @PostMapping(
            value = "/import",
            consumes = "text/plain",
            produces = "application/json"
    )
    public ResponseEntity<ImportReport> importar(
            @org.springframework.web.bind.annotation.RequestBody String caminho)
            throws IOException {

        // Remove espaços e quebras de linha desnecessários.
        Path caminhoArquivo = Path.of(caminho.trim());

        // Executa a importação e retorna o relatório.
        ImportReport relatorio =
                importService.importarMovimentosPorNivel(caminhoArquivo);

        // Retorna o relatório em JSON.
        return ResponseEntity.ok(relatorio);
    }



}
