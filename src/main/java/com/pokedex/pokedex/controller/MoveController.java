package com.pokedex.pokedex.controller;

// Importa a entidade que representa um movimento Pokémon.
import com.pokedex.pokedex.model.Move;

// Importa o repositório responsável pelas consultas ao banco de dados.
import com.pokedex.pokedex.repository.MoveRepository;

// Importa o serviço responsável pela importação dos movimentos.
import com.pokedex.pokedex.service.MoveImportService;

// Importa as classes utilizadas para construir respostas HTTP.
import org.springframework.http.ResponseEntity;

// Importa as anotações utilizadas para definir os endpoints.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa a classe utilizada para tratar erros de leitura de arquivos.
import java.io.IOException;

// Importa as classes utilizadas para trabalhar com listas e resultados opcionais.
import java.util.List;
import java.util.Optional;

// Indica ao Spring que esta classe disponibiliza endpoints HTTP.
@RestController

// Define o prefixo comum de todas as rotas deste controller.
@RequestMapping("/api/moves")
public class MoveController {

    // Repositório utilizado para consultar os movimentos cadastrados.
    private final MoveRepository moveRepository;

    // Serviço utilizado para importar os movimentos.
    private final MoveImportService moveImportService;

    // Recebe as dependências necessárias por injeção de dependência.
    public MoveController(
            MoveRepository moveRepository,
            MoveImportService moveImportService) {
        this.moveRepository = moveRepository;
        this.moveImportService = moveImportService;
    }

    // GET /api/moves
    // Retorna todos os movimentos cadastrados no banco de dados.
    @GetMapping
    public List<Move> listarTodos() {
        return moveRepository.findAll();
    }

    // GET /api/moves/{name}
    // Busca um movimento pelo nome interno, como FIRE_PUNCH.
    @GetMapping("/{name}")
    public ResponseEntity<Move> buscarPorNome(@PathVariable String name) {

        // Pesquisa o movimento usando o método definido no repositório.
        Optional<Move> movimento = moveRepository.findByName(name);

        // Retorna HTTP 200 se encontrar o movimento ou HTTP 404 se não encontrar.
        return movimento.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/moves/importar
    // Importa os movimentos usando o serviço existente.
    @PostMapping("/importar")
    public ResponseEntity<String> importarMovimentos() {

        try {
            // Executa a importação e recebe a quantidade de movimentos processados.
            int quantidade = moveImportService.importarMovimentos();

            // Retorna HTTP 200 com a quantidade processada.
            return ResponseEntity.ok(
                    "Importação concluída. Movimentos processados: " + quantidade
            );

        } catch (IOException erro) {
            // Retorna HTTP 500 se ocorrer um problema ao ler o arquivo.
            return ResponseEntity.internalServerError().body(
                    "Erro ao importar movimentos: " + erro.getMessage()
            );
        }
    }
}
