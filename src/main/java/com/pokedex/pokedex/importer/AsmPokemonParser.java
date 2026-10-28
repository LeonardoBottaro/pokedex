
package com.pokedex.pokedex.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AsmPokemonParser {

    private static final Path PASTA_BASE_STATS = Path.of(
            "polishedcrystal", "data", "pokemon", "base_stats"
    );

    private static final Path ARQUIVO_JSON = Path.of("pokemon.json");

    private static final Path ARQUIVO_ORDEM_JOHTO = Path.of(
            "polishedcrystal", "data", "pokemon", "dex_order_new.asm"
    );

    // Captura total, HP, ataque, defesa, ataque especial,
    // defesa especial e velocidade, nessa ordem.
    private static final Pattern STATS_PATTERN = Pattern.compile(
            "^\\s*bst\\s+(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)"
                    + "\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)"
                    + "\\s*,\\s*(\\d+)\\s*(?:;.*)?$"
    );

    // Aceita um ou dois tipos e comentários após a declaração.
    private static final Pattern TYPE_PATTERN = Pattern.compile(
            "^\\s*db\\s+([A-Z0-9_]+)"
                    + "(?:\\s*,\\s*([A-Z0-9_]+))?"
                    + "\\s*;\\s*type\\b.*$"
    );

    // Captura os dois identificadores da declaração de itens.
    private static final Pattern ITEMS_PATTERN = Pattern.compile(
            "^\\s*db\\s+([A-Z0-9_]+)\\s*,\\s*([A-Z0-9_]+)"
                    + "\\s*;\\s*held items\\b.*$"
    );

    // Os dados de cada Pokémon que serão escritos no JSON.
    // Integer permite deixar o número de Johto nulo quando não existir.
    public record PokemonData(
            String name,
            int total,
            int hp,
            int attack,
            int defense,
            int speed,
            int specialAttack,
            int specialDefense,
            String type1,
            String type2,
            String item1,
            String item2,
            Integer johtoDexNumber
    ) {
    }

    // Lê a ordem da Pokédex de Johto.
    private static Map<String, Integer> lerOrdemJohto(
            Path arquivo) throws IOException {

        Map<String, Integer> numeros = new HashMap<>();

        Pattern padrao = Pattern.compile(
                "^\\s*dp\\s+([A-Z0-9_]+)\\s*$"
        );

        int numero = 0;

        if (!Files.isRegularFile(arquivo)) {
            System.err.println(
                    "Aviso: arquivo da Pokédex não encontrado: " + arquivo
            );
            return numeros;
        }

        for (String linha : Files.readAllLines(
                arquivo, StandardCharsets.UTF_8)) {

            Matcher matcher = padrao.matcher(linha);

            if (matcher.matches()) {
                numero++;

                String nome = matcher.group(1).toLowerCase(Locale.ROOT);
                numeros.put(nome, numero);
            }
        }

        return numeros;
    }

    // Extrai os dados de um arquivo individual.
    public static PokemonData parse(Path arquivo) throws IOException {

        String nomeArquivo = arquivo.getFileName().toString();

        String nome = nomeArquivo.substring(
                0, nomeArquivo.length() - ".asm".length()
        ).toLowerCase(Locale.ROOT);

        Integer total = null;
        int[] stats = null;

        String type1 = null;
        String type2 = null;

        String item1 = null;
        String item2 = null;

        for (String linha : Files.readAllLines(
                arquivo, StandardCharsets.UTF_8)) {

            Matcher statsMatcher = STATS_PATTERN.matcher(linha);
            Matcher typeMatcher = TYPE_PATTERN.matcher(linha);
            Matcher itemsMatcher = ITEMS_PATTERN.matcher(linha);

            if (statsMatcher.matches()) {

                total = Integer.parseInt(statsMatcher.group(1));

                // A ordem do ASM é:
                // HP, ataque, defesa, ataque especial,
                // defesa especial e velocidade.
                stats = new int[] {
                        Integer.parseInt(statsMatcher.group(2)),
                        Integer.parseInt(statsMatcher.group(3)),
                        Integer.parseInt(statsMatcher.group(4)),
                        Integer.parseInt(statsMatcher.group(5)),
                        Integer.parseInt(statsMatcher.group(6)),
                        Integer.parseInt(statsMatcher.group(7))
                };
            }

            if (typeMatcher.matches()) {
                type1 = typeMatcher.group(1).toLowerCase(Locale.ROOT);

                type2 = typeMatcher.group(2) == null
                        ? null
                        : typeMatcher.group(2).toLowerCase(Locale.ROOT);

                if (type1.equals(type2)) {
                    type2 = null;
                }
            }

            if (itemsMatcher.matches()) {
                item1 = normalizarValor(itemsMatcher.group(1));
                item2 = normalizarValor(itemsMatcher.group(2));
            }
        }

        // Atributos e primeiro tipo são necessários para cadastrar
        // o Pokémon na estrutura atual do banco.
        if (total == null || stats == null || type1 == null) {
            throw new IllegalArgumentException(
                    "Atributos ou tipos ausentes no arquivo: " + arquivo
            );
        }

        int totalCalculado = 0;

        for (int stat : stats) {
            totalCalculado += stat;
        }

        if (totalCalculado != total) {
            throw new IllegalArgumentException(
                    "Total inválido para " + nome
                            + ": declarado=" + total
                            + ", calculado=" + totalCalculado
            );
        }

        return new PokemonData(
                nome,
                total,
                stats[0], // HP
                stats[1], // Ataque
                stats[2], // Defesa
                stats[5], // Velocidade
                stats[3], // Ataque especial
                stats[4], // Defesa especial
                type1,
                type2,
                item1,
                item2,
                null // Será preenchido com a ordem da Pokédex, se disponível.
        );
    }

    // Converte constantes de itens em minúsculas sem descartar
    // marcadores especiais definidos pelo próprio jogo.
    private static String normalizarValor(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim().toLowerCase(Locale.ROOT);
    }

    // Associa o número da Pokédex quando houver correspondência.
    private static PokemonData adicionarNumeroJohto(
            PokemonData pokemon,
            Map<String, Integer> numerosJohto) {

        Integer numero = numerosJohto.get(
                pokemon.name().toLowerCase(Locale.ROOT)
        );

        return new PokemonData(
                pokemon.name(),
                pokemon.total(),
                pokemon.hp(),
                pokemon.attack(),
                pokemon.defense(),
                pokemon.speed(),
                pokemon.specialAttack(),
                pokemon.specialDefense(),
                pokemon.type1(),
                pokemon.type2(),
                pokemon.item1(),
                pokemon.item2(),
                numero
        );
    }

    public static void main(String[] args) throws IOException {

        if (!Files.isDirectory(PASTA_BASE_STATS)) {
            throw new IOException(
                    "Pasta base_stats não encontrada: "
                            + PASTA_BASE_STATS.toAbsolutePath()
            );
        }

        Map<String, Integer> numerosJohto =
                lerOrdemJohto(ARQUIVO_ORDEM_JOHTO);

        List<PokemonData> pokemons = new ArrayList<>();

        int erros = 0;
        int arquivosEncontrados = 0;

        // Files.walk percorre a pasta e todas as suas subpastas.
        try (Stream<Path> arquivos = Files.walk(PASTA_BASE_STATS)) {

            List<Path> listaArquivos = arquivos
                    .filter(Files::isRegularFile)
                    .filter(caminho -> caminho.getFileName()
                            .toString().toLowerCase(Locale.ROOT)
                            .endsWith(".asm"))
                    .sorted()
                    .toList();

            arquivosEncontrados = listaArquivos.size();

            for (Path caminho : listaArquivos) {
                try {
                    PokemonData pokemon = parse(caminho);

                    // Formas ausentes na ordem de Johto também são exportadas.
                    // Nesse caso, johtoDexNumber fica null.
                    pokemons.add(
                            adicionarNumeroJohto(pokemon, numerosJohto)
                    );

                } catch (IOException | IllegalArgumentException e) {
                    erros++;

                    System.err.println(
                            "Erro em " + caminho + ": " + e.getMessage()
                    );
                }
            }
        }

        // Evita substituir o JSON existente por uma lista vazia
        // se todos os arquivos falharem na leitura.
        if (pokemons.isEmpty()) {
            throw new IOException(
                    "Nenhum Pokémon foi processado. "
                            + "O arquivo JSON existente foi preservado."
            );
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        mapper.writeValue(ARQUIVO_JSON.toFile(), pokemons);

        System.out.println("\n===== RESUMO DA EXPORTAÇÃO =====");
        System.out.println("Arquivos ASM encontrados: " + arquivosEncontrados);
        System.out.println("Pokémon exportados: " + pokemons.size());
        System.out.println("Arquivos com erro: " + erros);
        System.out.println(
                "Arquivo JSON: " + ARQUIVO_JSON.toAbsolutePath()
        );
    }
}
