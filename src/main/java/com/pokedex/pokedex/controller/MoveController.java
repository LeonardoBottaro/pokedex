
package com.pokedex.pokedex.controller;

import com.pokedex.pokedex.model.Move;
import com.pokedex.pokedex.repository.MoveRepository;
import com.pokedex.pokedex.service.MoveImportService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/moves")
public class MoveController {

    private final MoveRepository moveRepository;
    private final MoveImportService moveImportService;

    public MoveController(
            MoveRepository moveRepository,
            MoveImportService moveImportService) {
        this.moveRepository = moveRepository;
        this.moveImportService = moveImportService;
    }

    // GET /api/moves
    // Lista todos os movimentos.
    @GetMapping
    public List<Move> listarTodos() {
        return moveRepository.findAll();
    }

    // GET /api/moves/{name}
    // Busca pelo nome exato, ignorando maiúsculas/minúsculas.
    @GetMapping("/{name}")
    public ResponseEntity<Move> buscarPorNome(@PathVariable String name) {
        Optional<Move> movimento = moveRepository.findByNameIgnoreCase(name);

        return movimento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // GET /api/moves/buscar?nome=fire
    // GET /api/moves/buscar?tipo=FIRE
    // GET /api/moves/buscar?categoria=SPECIAL
    // Os parâmetros podem ser combinados.
    @GetMapping("/buscar")
    public List<Move> buscar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String categoria) {

        // Sem filtros, retorna todos os movimentos.
        if (vazio(nome) && vazio(tipo) && vazio(categoria)) {
            return listarTodos();
        }

        // Começa com todos os movimentos e aplica os filtros informados.
        return moveRepository.findAll().stream()
                .filter(m -> vazio(nome)
                        || (m.getName() != null
                        && m.getName().toLowerCase()
                        .contains(nome.trim().toLowerCase())))
                .filter(m -> vazio(tipo)
                        || (m.getType() != null
                        && m.getType().equalsIgnoreCase(tipo.trim())))
                .filter(m -> vazio(categoria)
                        || (m.getCategory() != null
                        && m.getCategory().equalsIgnoreCase(categoria.trim())))
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .toList();
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    // POST /api/moves/importar
    // Mantém a importação existente.
    @PostMapping("/importar")
    public ResponseEntity<String> importarMovimentos() {
        try {
            int quantidade = moveImportService.importarMovimentos();

            return ResponseEntity.ok(
                    "Importação concluída. Movimentos processados: " + quantidade
            );

        } catch (IOException erro) {
            return ResponseEntity.internalServerError().body(
                    "Erro ao importar movimentos: " + erro.getMessage()
            );
        }
    }
}
