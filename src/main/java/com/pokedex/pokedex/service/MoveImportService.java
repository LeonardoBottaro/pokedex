package com.pokedex.pokedex.service;

// Importa a entidade que representa os movimentos.
import com.pokedex.pokedex.model.Move;

// Importa o repositório responsável por acessar a tabela de movimentos.
import com.pokedex.pokedex.repository.MoveRepository;

// Importa a classe responsável por converter JSON em objetos Java.
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

// Importa as anotações utilizadas para registrar o serviço e controlar transações.
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importa as classes utilizadas para ler arquivos e caminhos.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Importa a lista e outras estruturas de coleção.
import java.util.List;
import java.util.Optional;

// Indica que esta classe contém regras de negócio gerenciadas pelo Spring.
@Service
public class MoveImportService {

    // Repositório utilizado para consultar e salvar os movimentos.
    private final MoveRepository moveRepository;

    // Conversor utilizado para transformar o JSON em objetos Java.
    private final ObjectMapper objectMapper;

    // Recebe automaticamente as dependências necessárias para o serviço.
    public MoveImportService(
            MoveRepository moveRepository,
            ObjectMapper objectMapper) {
        this.moveRepository = moveRepository;
        this.objectMapper = objectMapper;
    }

    // Executa a importação dentro de uma transação do banco de dados.
    @Transactional
    public int importarMovimentos() throws IOException {

        // Define o caminho do JSON gerado pelo MoveParser.
        Path caminhoArquivo = Path.of("moves.json");

        // Interrompe a importação se o arquivo não existir.
        if (!Files.isRegularFile(caminhoArquivo)) {
            throw new IOException(
                    "Arquivo moves.json não encontrado em: "
                            + caminhoArquivo.toAbsolutePath()
            );
        }

        // Converte o conteúdo JSON em uma lista de movimentos.
        List<Move> movimentos = objectMapper.readValue(
                caminhoArquivo.toFile(),
                new TypeReference<List<Move>>() {
                }
        );

        // Percorre todos os movimentos encontrados no arquivo.
        for (Move movimento : movimentos) {

            // Procura um registro existente com o mesmo nome.
            Optional<Move> movimentoExistente =
                    moveRepository.findByName(movimento.getName());

            // Reutiliza o identificador para atualizar o registro existente.
            movimentoExistente.ifPresent(
                    existente -> movimento.setId(existente.getId())
            );

            // Insere o movimento novo ou atualiza o movimento existente.
            moveRepository.save(movimento);
        }

        // Retorna a quantidade de registros processados.
        return movimentos.size();
    }
}
