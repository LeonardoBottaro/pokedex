package com.pokedex.pokedex.controller;

// Importa o serviço responsável pela importação dos movimentos.
import com.pokedex.pokedex.service.MoveImportService;

// Importa as anotações utilizadas para criar endpoints HTTP.
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Importa a classe utilizada para tratar erros de leitura de arquivos.
import java.io.IOException;

// Indica que esta classe recebe requisições HTTP e retorna respostas.
@RestController

// Define o prefixo das rotas deste controller.
@RequestMapping("/api/moves")
public class MoveController {

    // Serviço responsável por importar os movimentos para o banco.
    private final MoveImportService moveImportService;

    // Recebe o serviço automaticamente por injeção de dependência.
    public MoveController(MoveImportService moveImportService) {
        this.moveImportService = moveImportService;
    }

    // Define o endpoint POST /api/moves/importar.
    @PostMapping("/importar")
    public ResponseEntity<String> importarMovimentos() {

        try {
            // Executa a importação e recebe a quantidade processada.
            int quantidade = moveImportService.importarMovimentos();

            // Retorna uma resposta HTTP 200 com o resultado da importação.
            return ResponseEntity.ok(
                    "Importação concluída. Movimentos processados: " + quantidade
            );

        } catch (IOException erro) {
            // Retorna HTTP 500 quando ocorre um problema com o arquivo JSON.
            return ResponseEntity.internalServerError().body(
                    "Erro ao importar movimentos: " + erro.getMessage()
            );
        }
    }
}
