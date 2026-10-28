
package com.pokedex.pokedex.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class PokemonTmMoveParser {

    private static final Pattern POKEMON_PATTERN =
            Pattern.compile("^\\s*abilities_for\\s+(\\w+)\\s*,");

    private static final Pattern TMHM_PATTERN =
            Pattern.compile("^\\s*tmhm\\s+(.+?)\\s*$");

    public List<TmMoveData> parse(Path caminho) throws IOException {
        if (!Files.exists(caminho)) {
            throw new IOException("Caminho não encontrado: " + caminho);
        }

        if (Files.isRegularFile(caminho)) {
            return parseArquivo(caminho);
        }

        if (!Files.isDirectory(caminho)) {
            throw new IOException("Caminho inválido: " + caminho);
        }

        List<TmMoveData> resultados = new ArrayList<>();

        try (Stream<Path> arquivos = Files.walk(caminho)) {
            List<Path> arquivosAsm = arquivos
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString()
                            .toLowerCase()
                            .endsWith(".asm"))
                    .sorted(Comparator.naturalOrder())
                    .toList();

            for (Path arquivo : arquivosAsm) {
                resultados.addAll(parseArquivo(arquivo));
            }
        }

        return resultados;
    }

    private List<TmMoveData> parseArquivo(Path arquivo)
            throws IOException {
        List<TmMoveData> resultados = new ArrayList<>();
        String pokemonAtual = null;

        try (BufferedReader leitor = Files.newBufferedReader(arquivo)) {
            String linha;

            while ((linha = leitor.readLine()) != null) {
                String linhaLimpa = linha.trim();

                if (linhaLimpa.isEmpty() || linhaLimpa.startsWith(";")) {
                    continue;
                }

                int inicioComentario = linha.indexOf(';');
                if (inicioComentario >= 0) {
                    linha = linha.substring(0, inicioComentario);
                }

                Matcher pokemonMatcher = POKEMON_PATTERN.matcher(linha);

                if (pokemonMatcher.find()) {
                    pokemonAtual = pokemonMatcher.group(1);
                    continue;
                }

                Matcher tmhmMatcher = TMHM_PATTERN.matcher(linha);

                if (tmhmMatcher.find() && pokemonAtual != null) {
                    String[] movimentos = tmhmMatcher.group(1).split(",");

                    for (String movimento : movimentos) {
                        String nomeMovimento = movimento.trim();

                        if (!nomeMovimento.isEmpty()) {
                            resultados.add(new TmMoveData(
                                    pokemonAtual,
                                    nomeMovimento
                            ));
                        }
                    }
                }
            }
        }

        return resultados;
    }

    public static class TmMoveData {
        private final String pokemonName;
        private final String moveName;

        public TmMoveData(String pokemonName, String moveName) {
            this.pokemonName = pokemonName;
            this.moveName = moveName;
        }

        public String getPokemonName() {
            return pokemonName;
        }

        public String getMoveName() {
            return moveName;
        }
    }
}
