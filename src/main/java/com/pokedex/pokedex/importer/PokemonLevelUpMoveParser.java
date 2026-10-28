package com.pokedex.pokedex.importer;

// Importa classes para leitura de arquivos.
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Importa estruturas para armazenar os resultados.
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Responsável por interpretar os movimentos aprendidos por nível no arquivo ASM.
public class PokemonLevelUpMoveParser {

    // Identifica o início do bloco de um Pokémon.
    private static final Pattern POKEMON_PATTERN =
            Pattern.compile("^\\s*evos_attacks\\s+(\\w+)\\s*$");

    // Identifica o nível e o nome interno do movimento.
    private static final Pattern LEARNSET_PATTERN =
            Pattern.compile("^\\s*learnset\\s+(\\d+)\\s*,\\s*(\\w+)\\b");

    // Lê o arquivo ASM e retorna todos os movimentos encontrados.
    public List<LevelUpMoveData> parse(Path caminhoArquivo) throws IOException {

        // Cria a lista que receberá os dados extraídos.
        List<LevelUpMoveData> resultados = new ArrayList<>();

        // Guarda o nome do Pokémon do bloco atual.
        String pokemonAtual = null;

        // Abre o arquivo para leitura.
        try (BufferedReader leitor = Files.newBufferedReader(caminhoArquivo)) {

            // Percorre cada linha do arquivo.
            String linha;

            while ((linha = leitor.readLine()) != null) {

                // Ignora linhas vazias e comentários completos.
                String linhaLimpa = linha.trim();

                if (linhaLimpa.isEmpty() || linhaLimpa.startsWith(";")) {
                    continue;
                }

                // Remove comentários ao final da linha.
                int inicioComentario = linha.indexOf(';');

                if (inicioComentario >= 0) {
                    linha = linha.substring(0, inicioComentario);
                }

                // Verifica se começou um novo bloco de Pokémon.
                Matcher pokemonMatcher = POKEMON_PATTERN.matcher(linha);

                if (pokemonMatcher.find()) {
                    pokemonAtual = pokemonMatcher.group(1);
                    continue;
                }

                // Ignora linhas de movimentos antes de identificar um Pokémon.
                if (pokemonAtual == null) {
                    continue;
                }

                // Verifica se a linha define um movimento aprendido por nível.
                Matcher movimentoMatcher = LEARNSET_PATTERN.matcher(linha);

                if (movimentoMatcher.find()) {

                    // Extrai o nível e o nome interno do movimento.
                    int nivel = Integer.parseInt(movimentoMatcher.group(1));
                    String nomeMovimento = movimentoMatcher.group(2);

                    // Adiciona os dados extraídos à lista.
                    resultados.add(
                            new LevelUpMoveData(
                                    pokemonAtual,
                                    nomeMovimento,
                                    nivel
                            )
                    );
                }
            }
        }

        // Retorna todos os movimentos encontrados no arquivo.
        return resultados;
    }

    // Representa um movimento aprendido por um Pokémon em determinado nível.
    public static class LevelUpMoveData {

        // Nome interno do Pokémon.
        private final String pokemonName;

        // Nome interno do movimento.
        private final String moveName;

        // Nível em que o movimento é aprendido.
        private final int level;

        // Cria um registro temporário com os dados extraídos.
        public LevelUpMoveData(String pokemonName, String moveName, int level) {
            this.pokemonName = pokemonName;
            this.moveName = moveName;
            this.level = level;
        }

        // Retorna o nome interno do Pokémon.
        public String getPokemonName() {
            return pokemonName;
        }

        // Retorna o nome interno do movimento.
        public String getMoveName() {
            return moveName;
        }

        // Retorna o nível de aprendizado.
        public int getLevel() {
            return level;
        }
    }
}
