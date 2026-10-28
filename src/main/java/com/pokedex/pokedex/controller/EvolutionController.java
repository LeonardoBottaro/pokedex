
package com.pokedex.pokedex.controller;

import com.pokedex.pokedex.dto.EvolutionConditionResponse;
import com.pokedex.pokedex.dto.EvolutionNodeResponse;
import com.pokedex.pokedex.dto.EvolutionResponse;
import com.pokedex.pokedex.dto.EvolutionTreeResponse;
import com.pokedex.pokedex.model.Evolution;
import com.pokedex.pokedex.model.Pokemon;
import com.pokedex.pokedex.repository.EvolutionRepository;
import com.pokedex.pokedex.repository.PokemonRepository;
import com.pokedex.pokedex.service.EvolutionDescriptionService;
import com.pokedex.pokedex.service.EvolutionImportService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/evolutions")
public class EvolutionController {

    private final EvolutionRepository evolutionRepository;
    private final PokemonRepository pokemonRepository;
    private final EvolutionImportService evolutionImportService;
    private final EvolutionDescriptionService evolutionDescriptionService;

    public EvolutionController(
            EvolutionRepository evolutionRepository,
            PokemonRepository pokemonRepository,
            EvolutionImportService evolutionImportService,
            EvolutionDescriptionService evolutionDescriptionService) {

        this.evolutionRepository = evolutionRepository;
        this.pokemonRepository = pokemonRepository;
        this.evolutionImportService = evolutionImportService;
        this.evolutionDescriptionService = evolutionDescriptionService;
    }

    /**
     * Lista todas as relações de evolução individualmente.
     *
     * GET /api/evolutions
     */
    @GetMapping
    public List<EvolutionResponse> listarTodas() {
        return evolutionRepository.findAll()
                .stream()
                .map(this::converterParaResponse)
                .toList();
    }

    /**
     * Busca as evoluções anteriores, posteriores e a árvore
     * evolutiva completa do Pokémon informado.
     *
     * GET /api/evolutions/{pokemonName}
     */
    @GetMapping("/{pokemonName}")
    public EvolutionTreeResponse buscarPorPokemon(
            @PathVariable String pokemonName) {

        Pokemon pokemon = pokemonRepository
                .findByNameIgnoreCase(pokemonName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pokémon não encontrado: " + pokemonName
                ));

        // Evoluções que levam até o Pokémon consultado.
        List<EvolutionResponse> anteriores =
                evolutionRepository
                        .findByToPokemon_NameIgnoreCaseOrderByFromPokemon_NameAsc(
                                pokemon.getName())
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        // Evoluções diretas que partem do Pokémon consultado.
        // Mantém cada método alternativo como uma relação separada.
        List<EvolutionResponse> posteriores =
                evolutionRepository
                        .findByFromPokemon_NameIgnoreCaseOrderByToPokemon_NameAsc(
                                pokemon.getName())
                        .stream()
                        .map(this::converterParaResponse)
                        .toList();

        // Árvore agrupada por Pokémon de destino.
        EvolutionNodeResponse arvore = construirArvore(
                pokemon,
                new HashSet<>()
        );

        return new EvolutionTreeResponse(
                pokemon.getName(),
                anteriores,
                posteriores,
                arvore
        );
    }

    /**
     * Constrói a árvore evolutiva agrupando condições alternativas
     * que levam ao mesmo Pokémon.
     *
     * O conjunto caminhoAtual impede ciclos no caminho percorrido,
     * sem bloquear o mesmo Pokémon em ramos independentes.
     */
    private EvolutionNodeResponse construirArvore(
            Pokemon pokemon,
            Set<Integer> caminhoAtual) {

        Integer pokemonId = pokemon.getId();

        // Proteção contra ciclos.
        if (caminhoAtual.contains(pokemonId)) {
            return new EvolutionNodeResponse(
                    pokemon.getName(),
                    List.of(),
                    List.of()
            );
        }

        Set<Integer> novoCaminho = new HashSet<>(caminhoAtual);
        novoCaminho.add(pokemonId);

        List<Evolution> evolucoes =
                evolutionRepository
                        .findByFromPokemon_NameIgnoreCaseOrderByToPokemon_NameAsc(
                                pokemon.getName());

        /*
         * Agrupa as relações pelo ID do Pokémon de destino.
         * Exemplo: Eevee -> Espeon terá um único nó contendo
         * todas as condições que permitem essa evolução.
         */
        Map<Integer, List<Evolution>> evolucoesAgrupadas =
                evolucoes.stream()
                        .collect(Collectors.groupingBy(
                                evolution -> evolution
                                        .getToPokemon()
                                        .getId(),
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        List<EvolutionNodeResponse> filhos = new ArrayList<>();

        for (List<Evolution> grupo : evolucoesAgrupadas.values()) {

            Evolution primeira = grupo.get(0);
            Pokemon destino = primeira.getToPokemon();

            // Evita percorrer novamente um Pokémon já presente no caminho.
            if (novoCaminho.contains(destino.getId())) {
                continue;
            }

            /*
             * Cria as condições de evolução, incluindo os códigos
             * originais e suas descrições em português.
             *
             * distinct() evita condições idênticas duplicadas.
             */
            List<EvolutionConditionResponse> condicoes =
                    grupo.stream()
                            .map(evolution ->
                                    new EvolutionConditionResponse(
                                            evolution.getMethod(),
                                            evolution.getConditionValue(),
                                            evolutionDescriptionService.descrever(
                                                    evolution.getMethod(),
                                                    evolution.getConditionValue()
                                            )
                                    )
                            )
                            .distinct()
                            .toList();

            // Calcula os descendentes uma única vez para este destino.
            EvolutionNodeResponse subArvore = construirArvore(
                    destino,
                    novoCaminho
            );

            filhos.add(new EvolutionNodeResponse(
                    destino.getName(),
                    condicoes,
                    subArvore.evolucoesPosteriores()
            ));
        }

        return new EvolutionNodeResponse(
                pokemon.getName(),
                List.of(),
                filhos
        );
    }

    /**
     * Importa as evoluções do arquivo evos_attacks.asm.
     *
     * POST /api/evolutions/importar
     */
    @PostMapping("/importar")
    public Map<String, Object> importar() throws IOException {

        Path arquivo = Path.of(
                "H:\\Projetos VSCode\\pokedex\\polishedcrystal",
                "data",
                "pokemon",
                "evos_attacks.asm"
        );

        return evolutionImportService.importar(arquivo);
    }

    /**
     * Converte uma relação de evolução para o DTO utilizado
     * pelos endpoints de listagem.
     */
    private EvolutionResponse converterParaResponse(
            Evolution evolution) {

        return new EvolutionResponse(
                evolution.getFromPokemon().getName(),
                evolution.getToPokemon().getName(),
                evolution.getMethod(),
                evolution.getConditionValue()
        );
    }
}
