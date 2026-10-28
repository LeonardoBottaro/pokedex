
package com.pokedex.pokedex.service;

import com.pokedex.pokedex.importer.PokemonLevelUpMoveParser;
import com.pokedex.pokedex.importer.PokemonLevelUpMoveParser.LevelUpMoveData;
import com.pokedex.pokedex.model.Move;
import com.pokedex.pokedex.model.Pokemon;
import com.pokedex.pokedex.model.PokemonLevelUpMove;
import com.pokedex.pokedex.repository.MoveRepository;
import com.pokedex.pokedex.repository.PokemonLevelUpMoveRepository;
import com.pokedex.pokedex.repository.PokemonRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PokemonLevelUpMoveImportService {

    private final PokemonRepository pokemonRepository;
    private final MoveRepository moveRepository;
    private final PokemonLevelUpMoveRepository levelUpMoveRepository;

    private static final Map<String, String> NOMES_ESPECIAIS = criarMapaNomes();

    public PokemonLevelUpMoveImportService(
            PokemonRepository pokemonRepository,
            MoveRepository moveRepository,
            PokemonLevelUpMoveRepository levelUpMoveRepository) {
        this.pokemonRepository = pokemonRepository;
        this.moveRepository = moveRepository;
        this.levelUpMoveRepository = levelUpMoveRepository;
    }

    private static Map<String, String> criarMapaNomes() {
        Map<String, String> nomes = new LinkedHashMap<>();

        nomes.put("HoOh", "ho_oh");
        nomes.put("NidoranF", "nidoran_f");
        nomes.put("NidoranM", "nidoran_m");
        nomes.put("MimeJr", "mime_jr_");
        nomes.put("PorygonZ", "porygon_z");
        nomes.put("SirfetchD", "sirfetch_d");

        // O banco usa dois underscores nesses nomes.
        nomes.put("MrMime", "mr__mime");
        nomes.put("MrRime", "mr__rime");

        return Map.copyOf(nomes);
    }

    
    private String normalizarNomePokemon(String nomeInterno) {
        String nome = nomeInterno
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);

        if (nome.startsWith("mr_mime")) {
            nome = nome.replaceFirst("^mr_mime", "mr__mime");
        } else if (nome.startsWith("mr_rime")) {
            nome = nome.replaceFirst("^mr_rime", "mr__rime");
        }

        if (nome.equals("mime_jr")) {
            nome = "mime_jr_";
        }

        if (nome.equals("mewtwo")) {
            nome = "mewtwo_plain";
        }

        return nome;
    }


    private Pokemon buscarPokemon(String nomeInterno) {
        String nomeBanco = normalizarNomePokemon(nomeInterno);

        return pokemonRepository.findByName(nomeBanco).orElse(null);
    }

    @Transactional
    public ImportReport importarMovimentosPorNivel(Path caminhoArquivo)
            throws IOException {

        List<LevelUpMoveData> dados =
                new PokemonLevelUpMoveParser().parse(caminhoArquivo);

        int importados = 0;
        int pokemonNaoEncontrados = 0;
        int movimentosNaoEncontrados = 0;
        int duplicados = 0;

        Set<String> nomesPokemonNaoEncontrados = new LinkedHashSet<>();

        for (LevelUpMoveData dado : dados) {
            Pokemon pokemon = buscarPokemon(dado.getPokemonName());

            if (pokemon == null) {
                pokemonNaoEncontrados++;
                nomesPokemonNaoEncontrados.add(dado.getPokemonName());
                continue;
            }

            Move move = moveRepository.findByName(dado.getMoveName())
                    .orElse(null);

            if (move == null) {
                movimentosNaoEncontrados++;
                continue;
            }

            boolean jaExiste =
                    levelUpMoveRepository.existsByPokemonIdAndMoveIdAndLevel(
                            pokemon.getId(),
                            move.getId(),
                            dado.getLevel());

            if (jaExiste) {
                duplicados++;
                continue;
            }

            PokemonLevelUpMove registro = new PokemonLevelUpMove();
            registro.setPokemon(pokemon);
            registro.setMove(move);
            registro.setLevel(dado.getLevel());

            levelUpMoveRepository.save(registro);
            importados++;
        }

        return new ImportReport(
                dados.size(),
                importados,
                pokemonNaoEncontrados,
                movimentosNaoEncontrados,
                duplicados,
                nomesPokemonNaoEncontrados);
    }

    @Transactional(readOnly = true)
    public ImportReport diagnosticarMovimentosPorNivel(Path caminhoArquivo)
            throws IOException {

        List<LevelUpMoveData> dados =
                new PokemonLevelUpMoveParser().parse(caminhoArquivo);

        int pokemonNaoEncontrados = 0;
        int movimentosNaoEncontrados = 0;
        int duplicados = 0;
        int registrosValidos = 0;

        Set<String> nomesPokemonNaoEncontrados = new LinkedHashSet<>();

        for (LevelUpMoveData dado : dados) {
            Pokemon pokemon = buscarPokemon(dado.getPokemonName());

            if (pokemon == null) {
                pokemonNaoEncontrados++;
                nomesPokemonNaoEncontrados.add(dado.getPokemonName());
                continue;
            }

            Move move = moveRepository.findByName(dado.getMoveName())
                    .orElse(null);

            if (move == null) {
                movimentosNaoEncontrados++;
                continue;
            }

            boolean jaExiste =
                    levelUpMoveRepository.existsByPokemonIdAndMoveIdAndLevel(
                            pokemon.getId(),
                            move.getId(),
                            dado.getLevel());

            if (jaExiste) {
                duplicados++;
                continue;
            }

            registrosValidos++;
        }

        return new ImportReport(
                dados.size(),
                registrosValidos,
                pokemonNaoEncontrados,
                movimentosNaoEncontrados,
                duplicados,
                nomesPokemonNaoEncontrados);
    }

    public static class ImportReport {

        private final int totalEncontrado;
        private final int importados;
        private final int pokemonNaoEncontrados;
        private final int movimentosNaoEncontrados;
        private final int duplicados;
        private final Set<String> nomesPokemonNaoEncontrados;

        public ImportReport(
                int totalEncontrado,
                int importados,
                int pokemonNaoEncontrados,
                int movimentosNaoEncontrados,
                int duplicados,
                Set<String> nomesPokemonNaoEncontrados) {
            this.totalEncontrado = totalEncontrado;
            this.importados = importados;
            this.pokemonNaoEncontrados = pokemonNaoEncontrados;
            this.movimentosNaoEncontrados = movimentosNaoEncontrados;
            this.duplicados = duplicados;
            this.nomesPokemonNaoEncontrados =
                    new LinkedHashSet<>(nomesPokemonNaoEncontrados);
        }

        public int getTotalEncontrado() {
            return totalEncontrado;
        }

        public int getImportados() {
            return importados;
        }

        public int getPokemonNaoEncontrados() {
            return pokemonNaoEncontrados;
        }

        public int getMovimentosNaoEncontrados() {
            return movimentosNaoEncontrados;
        }

        public int getDuplicados() {
            return duplicados;
        }

        public Set<String> getNomesPokemonNaoEncontrados() {
            return nomesPokemonNaoEncontrados;
        }
    }
}
