package com.pokedex.pokedex.service;

import com.pokedex.pokedex.model.Pokemon;
import com.pokedex.pokedex.repository.PokemonRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
public class PokemonImportService {

    private static final Path PASTA_BASE_STATS = Path.of(
            "H:\\Projetos VSCode\\pokedex\\polishedcrystal",
            "data", "pokemon", "base_stats"
    );

    private static final Pattern PADRAO_BST = Pattern.compile(
            "(?m)^\\s*bst\\s+(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)"
                    + "\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)"
                    + "\\s*,\\s*(\\d+)"
    );

    private static final Pattern PADRAO_TIPO = Pattern.compile(
            "(?m)^\\s*db\\s+([A-Z_]+)\\s*,\\s*([A-Z_]+)"
                    + "\\s*;\\s*type\\b"
    );

    private static final Pattern PADRAO_ITEM = Pattern.compile(
            "(?m)^\\s*db\\s+([^;\\r\\n]+?)\\s*;\\s*held items\\b"
    );

    private final PokemonRepository pokemonRepository;

    public PokemonImportService(PokemonRepository pokemonRepository) {
        this.pokemonRepository = pokemonRepository;
    }

    @Transactional
    public String importarPokemon() throws IOException {

        if (!Files.isDirectory(PASTA_BASE_STATS)) {
            throw new IOException(
                    "Pasta base_stats não encontrada: " + PASTA_BASE_STATS
            );
        }

        int inseridos = 0;
        int atualizados = 0;
        int ignorados = 0;
        int erros = 0;

        StringBuilder relatorio = new StringBuilder();

        try (Stream<Path> arquivos = Files.walk(PASTA_BASE_STATS)) {

            for (Path arquivo : arquivos
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString()
                            .toLowerCase(Locale.ROOT).endsWith(".asm"))
                    .sorted()
                    .toList()) {

                try {
                    String conteudo = Files.readString(
                            arquivo, StandardCharsets.UTF_8
                    );

                    Pokemon pokemon = interpretarArquivo(arquivo, conteudo);

                    if (pokemon == null) {
                        ignorados++;
                        relatorio.append(
                                "\nIgnorado: ").append(arquivo.getFileName())
                                .append(" (dados obrigatórios não encontrados)");
                        continue;
                    }

                    Optional<Pokemon> existente =
                            pokemonRepository.findByName(pokemon.getName());

                    if (existente.isPresent()) {
                        Pokemon atual = existente.get();

                        atual.setTotal(pokemon.getTotal());
                        atual.setHp(pokemon.getHp());
                        atual.setAttack(pokemon.getAttack());
                        atual.setDefense(pokemon.getDefense());
                        atual.setSpeed(pokemon.getSpeed());
                        atual.setSpecialAttack(pokemon.getSpecialAttack());
                        atual.setSpecialDefense(pokemon.getSpecialDefense());
                        atual.setType1(pokemon.getType1());
                        atual.setType2(pokemon.getType2());
                        atual.setItem1(pokemon.getItem1());
                        atual.setItem2(pokemon.getItem2());

                        // Preserva o ID e o número da Pokédex existente.
                        pokemonRepository.save(atual);
                        atualizados++;
                    } else {
                        pokemonRepository.save(pokemon);
                        inseridos++;
                    }

                } catch (Exception e) {
                    erros++;
                    relatorio.append("\nErro em ")
                            .append(arquivo.getFileName())
                            .append(": ")
                            .append(e.getMessage());
                }
            }
        }

        return "Importação de base_stats concluída."
                + "\nInseridos: " + inseridos
                + "\nAtualizados: " + atualizados
                + "\nIgnorados: " + ignorados
                + "\nErros: " + erros
                + "\nDetalhes:" + relatorio;
    }

    private Pokemon interpretarArquivo(Path arquivo, String conteudo) {

        Matcher bst = PADRAO_BST.matcher(conteudo);
        Matcher tipos = PADRAO_TIPO.matcher(conteudo);

        if (!bst.find() || !tipos.find()) {
            return null;
        }

        Pokemon pokemon = new Pokemon();

        // O nome é derivado do nome do arquivo, sem a extensão .asm.
        String nome = arquivo.getFileName().toString();
        nome = nome.substring(0, nome.length() - 4);
        pokemon.setName(nome.toLowerCase(Locale.ROOT));

        // Formato: total, HP, ataque, defesa, ataque especial,
        // defesa especial e velocidade.
        pokemon.setTotal(Integer.parseInt(bst.group(1)));
        pokemon.setHp(Integer.parseInt(bst.group(2)));
        pokemon.setAttack(Integer.parseInt(bst.group(3)));
        pokemon.setDefense(Integer.parseInt(bst.group(4)));
        pokemon.setSpecialAttack(Integer.parseInt(bst.group(5)));
        pokemon.setSpecialDefense(Integer.parseInt(bst.group(6)));
        pokemon.setSpeed(Integer.parseInt(bst.group(7)));

        pokemon.setType1(normalizarConstante(tipos.group(1)));
        pokemon.setType2(normalizarConstante(tipos.group(2)));

        Matcher itens = PADRAO_ITEM.matcher(conteudo);

        if (itens.find()) {
            String[] valores = itens.group(1).split(",");

            if (valores.length >= 2) {
                pokemon.setItem1(normalizarItem(valores[0]));
                pokemon.setItem2(normalizarItem(valores[1]));
            }
        }

        return pokemon;
    }

    private String normalizarConstante(String valor) {
        return valor.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarItem(String valor) {
        String item = valor.trim().toLowerCase(Locale.ROOT);

        // Marcadores especiais não são itens comuns.
        if (item.startsWith("always_item_")) {
            return null;
        }

        return item;
    }
}
