
package com.pokedex.pokedex.service;

import com.pokedex.pokedex.model.Evolution;
import com.pokedex.pokedex.model.Pokemon;
import com.pokedex.pokedex.repository.EvolutionRepository;
import com.pokedex.pokedex.repository.PokemonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EvolutionImportService {

    private final PokemonRepository pokemonRepository;
    private final EvolutionRepository evolutionRepository;

    private static final Pattern BLOCO_PATTERN =
            Pattern.compile("^\\s*evos_attacks\\s+(\\S+)\\s*$",
                    Pattern.CASE_INSENSITIVE);

    private static final Pattern EVO_PATTERN =
            Pattern.compile("^\\s*evo_data\\s+(.+?)\\s*$",
                    Pattern.CASE_INSENSITIVE);

    public EvolutionImportService(
            PokemonRepository pokemonRepository,
            EvolutionRepository evolutionRepository) {
        this.pokemonRepository = pokemonRepository;
        this.evolutionRepository = evolutionRepository;
    }

    @Transactional
    public Map<String, Object> importar(Path arquivo) throws IOException {

        if (arquivo == null || !Files.exists(arquivo)) {
            throw new IOException("Arquivo não encontrado: " + arquivo);
        }

        List<String> linhas = Files.readAllLines(
                arquivo, StandardCharsets.UTF_8);

        int importadas = 0;
        int duplicadas = 0;
        int pokemonNaoEncontrados = 0;
        int evolucoesInvalidas = 0;

        List<String> detalhesNaoEncontrados = new ArrayList<>();
        List<String> detalhesInvalidos = new ArrayList<>();

        String pokemonOrigemAsm = null;

        for (int numeroLinha = 0; numeroLinha < linhas.size(); numeroLinha++) {

            String linhaOriginal = linhas.get(numeroLinha);

            // Remove comentários do ASM.
            String linha = linhaOriginal.split(";", 2)[0].trim();

            if (linha.isEmpty()) {
                continue;
            }

            Matcher blocoMatcher = BLOCO_PATTERN.matcher(linha);

            if (blocoMatcher.matches()) {
                pokemonOrigemAsm = blocoMatcher.group(1).trim();
                continue;
            }

            Matcher evoMatcher = EVO_PATTERN.matcher(linha);

            if (!evoMatcher.matches()) {
                continue;
            }

            if (pokemonOrigemAsm == null) {
                evolucoesInvalidas++;
                detalhesInvalidos.add(
                        "Linha " + (numeroLinha + 1)
                                + ": evo_data sem bloco evos_attacks: "
                                + linhaOriginal.trim());
                continue;
            }

            String[] partes = Arrays.stream(
                            evoMatcher.group(1).split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);

            if (partes.length < 2) {
                evolucoesInvalidas++;
                detalhesInvalidos.add(
                        "Linha " + (numeroLinha + 1)
                                + ": argumentos insuficientes: "
                                + linhaOriginal.trim());
                continue;
            }

            String metodo = partes[0].toUpperCase(Locale.ROOT);
            int indiceDestino = obterIndiceDestino(metodo);

            if (indiceDestino < 0) {
                evolucoesInvalidas++;
                detalhesInvalidos.add(
                        "Método não configurado na linha "
                                + (numeroLinha + 1) + ": "
                                + linhaOriginal.trim());
                continue;
            }

            if (partes.length <= indiceDestino) {
                evolucoesInvalidas++;
                detalhesInvalidos.add(
                        "Faltam argumentos para " + metodo
                                + " na linha " + (numeroLinha + 1)
                                + ": " + linhaOriginal.trim());
                continue;
            }

            String destinoAsm = partes[indiceDestino];
            String formaDestino = null;

            // Argumentos após o destino podem especificar a forma.
            for (int i = indiceDestino + 1; i < partes.length; i++) {
                String extra = partes[i].trim();

                if (extra.toUpperCase(Locale.ROOT).endsWith("_FORM")) {
                    if (!extra.equalsIgnoreCase("NO_FORM")) {
                        formaDestino = extra.substring(
                                        0, extra.length() - "_FORM".length())
                                .toLowerCase(Locale.ROOT);
                    }
                }
            }

            String condicao = Arrays.stream(
                            Arrays.copyOfRange(partes, 1, indiceDestino))
                    .collect(Collectors.joining(", "));

            Optional<Pokemon> origemOpt =
                    encontrarPokemon(pokemonOrigemAsm, null);

            Optional<Pokemon> destinoOpt =
                    encontrarPokemon(destinoAsm, formaDestino);

            if (origemOpt.isEmpty() || destinoOpt.isEmpty()) {
                pokemonNaoEncontrados++;

                detalhesNaoEncontrados.add(
                        "Origem: " + pokemonOrigemAsm
                                + " -> Destino: " + destinoAsm
                                + " (forma: "
                                + (formaDestino == null
                                ? "nenhuma" : formaDestino)
                                + ") | Origem encontrada: "
                                + origemOpt.isPresent()
                                + " | Destino encontrado: "
                                + destinoOpt.isPresent());

                continue;
            }

            Pokemon origem = origemOpt.get();
            Pokemon destino = destinoOpt.get();

            boolean existe = evolutionRepository
                    .existsByFromPokemon_IdAndToPokemon_IdAndMethodAndConditionValue(
                            origem.getId(),
                            destino.getId(),
                            metodo,
                            condicao);

            if (existe) {
                duplicadas++;
                continue;
            }

            Evolution evolution = new Evolution();
            evolution.setFromPokemon(origem);
            evolution.setToPokemon(destino);
            evolution.setMethod(metodo);
            evolution.setConditionValue(condicao);

            evolutionRepository.save(evolution);
            importadas++;
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("arquivo", arquivo.toAbsolutePath().toString());
        resultado.put("importadas", importadas);
        resultado.put("duplicadas", duplicadas);
        resultado.put("pokemonNaoEncontrados", pokemonNaoEncontrados);
        resultado.put("evolucoesInvalidas", evolucoesInvalidas);
        resultado.put("detalhesNaoEncontrados", detalhesNaoEncontrados);
        resultado.put("detalhesInvalidos", detalhesInvalidos);

        return resultado;
    }

    /**
     * Retorna o índice do argumento que contém o Pokémon de destino.
     * O método está no índice 0, pois "evo_data" já foi removido.
     */
    private int obterIndiceDestino(String metodo) {

        switch (metodo) {
            case "EVOLVE_HOLDING":
            case "EVOLVE_STAT":
                // Ex.: EVOLVE_STAT, 20, ATK_LT_DEF, HITMONCHAN
                // Ex.: EVOLVE_HOLDING, HARD_STONE, TR_ANYTIME, KLEAVOR
                return 3;

            case "EVOLVE_LEVEL":
            case "EVOLVE_ITEM":
            case "EVOLVE_MOVE":
            case "EVOLVE_LOCATION":
            case "EVOLVE_TRADE":
            case "EVOLVE_HAPPINESS":
            case "EVOLVE_CRIT":
            case "EVOLVE_PARTY":
                // Ex.: EVOLVE_HAPPINESS, TR_ANYTIME, CROBAT
                // Ex.: EVOLVE_CRIT, TR_ANYTIME, SIRFETCH_D, PLAIN_FORM
                // Ex.: EVOLVE_PARTY, REMORAID, MANTINE
                return 2;

            default:
                return -1;
        }
    }

    /**
     * Procura o Pokémon no banco usando o nome ASM normalizado.
     * Quando uma forma é informada, tenta primeiro o nome com a forma.
     * Se não existir, tenta o nome sem sufixo de forma.
     */
    private Optional<Pokemon> encontrarPokemon(
            String nomeAsm, String forma) {

        String nomeNormalizado = normalizarNome(nomeAsm);

        // No banco, MimeJr está cadastrado como "mime_jr_".
        if (nomeNormalizado.equals("mime_jr")) {
            nomeNormalizado = "mime_jr_";
        }

        if (forma != null && !forma.isBlank()) {
            String nomeComForma = nomeNormalizado + "_" + forma;

            Optional<Pokemon> comForma =
                    pokemonRepository.findByNameIgnoreCase(nomeComForma);

            if (comForma.isPresent()) {
                return comForma;
            }
        }

        Optional<Pokemon> direto =
                pokemonRepository.findByNameIgnoreCase(nomeNormalizado);

        if (direto.isPresent()) {
            return direto;
        }

        // Compatibilidade com registros cuja forma padrão
        // está armazenada com o sufixo "_plain".
        if (!nomeNormalizado.endsWith("_plain")
                && !nomeNormalizado.endsWith("_galarian")
                && !nomeNormalizado.endsWith("_hisuian")
                && !nomeNormalizado.endsWith("_alolan")) {

            Optional<Pokemon> plain =
                    pokemonRepository.findByNameIgnoreCase(
                            nomeNormalizado + "_plain");

            if (plain.isPresent()) {
                return plain;
            }
        }

        return Optional.empty();
    }

    /**
     * Converte identificadores ASM como MimeJr, MeowthGalarian
     * e MR__MIME para o padrão de nomes usado no banco.
     */
    private String normalizarNome(String nome) {

        if (nome == null || nome.isBlank()) {
            return "";
        }

        String normalizado = nome.trim()
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                .toLowerCase(Locale.ROOT);

        // O projeto usa "mr__mime" para manter o identificador
        // com dois underscores.
        if (normalizado.equals("mr_mime")) {
            return "mr__mime";
        }

        if (normalizado.equals("mr_mime_plain")) {
            return "mr__mime_plain";
        }

        if (normalizado.equals("mr_mime_galarian")) {
            return "mr__mime_galarian";
        }

        return normalizado;
    }
}
