
package com.pokedex.pokedex.service;

import com.pokedex.pokedex.importer.PokemonTmMoveParser;
import com.pokedex.pokedex.importer.PokemonTmMoveParser.TmMoveData;
import com.pokedex.pokedex.model.Move;
import com.pokedex.pokedex.model.Pokemon;
import com.pokedex.pokedex.model.PokemonTmMove;
import com.pokedex.pokedex.repository.MoveRepository;
import com.pokedex.pokedex.repository.PokemonRepository;
import com.pokedex.pokedex.repository.PokemonTmMoveRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class PokemonTmMoveImportService {

    private final PokemonRepository pokemonRepository;
    private final MoveRepository moveRepository;
    private final PokemonTmMoveRepository pokemonTmMoveRepository;

    public PokemonTmMoveImportService(
            PokemonRepository pokemonRepository,
            MoveRepository moveRepository,
            PokemonTmMoveRepository pokemonTmMoveRepository) {
        this.pokemonRepository = pokemonRepository;
        this.moveRepository = moveRepository;
        this.pokemonTmMoveRepository = pokemonTmMoveRepository;
    }

    @Transactional
    public ImportReport importar(Path caminho) throws IOException {
        List<TmMoveData> dados = new PokemonTmMoveParser().parse(caminho);

        ImportReport relatorio = new ImportReport();
        relatorio.totalEncontrado = dados.size();

        Set<String> pokemonNaoEncontrados = new LinkedHashSet<>();
        Set<String> movimentosNaoEncontrados = new LinkedHashSet<>();

        for (TmMoveData dado : dados) {
            String nomePokemon = normalizarNomePokemon(dado.getPokemonName());
            String nomeMovimento = dado.getMoveName();

            Optional<Pokemon> pokemonOpt =
                    pokemonRepository.findByName(nomePokemon);

            if (pokemonOpt.isEmpty()) {
                relatorio.pokemonNaoEncontrados++;
                pokemonNaoEncontrados.add(dado.getPokemonName());
                continue;
            }

            Optional<Move> movimentoOpt = buscarMovimento(nomeMovimento);

            if (movimentoOpt.isEmpty()) {
                relatorio.movimentosNaoEncontrados++;
                movimentosNaoEncontrados.add(nomeMovimento);
                continue;
            }

            Pokemon pokemon = pokemonOpt.get();
            Move movimento = movimentoOpt.get();

            if (pokemonTmMoveRepository.existsByPokemon_IdAndMove_Id(
                    pokemon.getId(), movimento.getId())) {
                relatorio.duplicados++;
                continue;
            }

            PokemonTmMove registro = new PokemonTmMove();
            registro.setPokemon(pokemon);
            registro.setMove(movimento);

            pokemonTmMoveRepository.save(registro);
            relatorio.importados++;
        }

        relatorio.nomesPokemonNaoEncontrados =
                new ArrayList<>(pokemonNaoEncontrados);
        relatorio.nomesMovimentosNaoEncontrados =
                new ArrayList<>(movimentosNaoEncontrados);

        return relatorio;
    }

    @Transactional(readOnly = true)
    public ImportReport diagnosticar(Path caminho) throws IOException {
        List<TmMoveData> dados = new PokemonTmMoveParser().parse(caminho);

        ImportReport relatorio = new ImportReport();
        relatorio.totalEncontrado = dados.size();

        Set<String> pokemonNaoEncontrados = new LinkedHashSet<>();
        Set<String> movimentosNaoEncontrados = new LinkedHashSet<>();

        for (TmMoveData dado : dados) {
            String nomePokemon = normalizarNomePokemon(dado.getPokemonName());
            String nomeMovimento = dado.getMoveName();

            Optional<Pokemon> pokemonOpt =
                    pokemonRepository.findByName(nomePokemon);

            if (pokemonOpt.isEmpty()) {
                relatorio.pokemonNaoEncontrados++;
                pokemonNaoEncontrados.add(dado.getPokemonName());
                continue;
            }

            Optional<Move> movimentoOpt = buscarMovimento(nomeMovimento);

            if (movimentoOpt.isEmpty()) {
                relatorio.movimentosNaoEncontrados++;
                movimentosNaoEncontrados.add(nomeMovimento);
                continue;
            }

            if (pokemonTmMoveRepository.existsByPokemon_IdAndMove_Id(
                    pokemonOpt.get().getId(), movimentoOpt.get().getId())) {
                relatorio.duplicados++;
            } else {
                relatorio.importados++;
            }
        }

        relatorio.nomesPokemonNaoEncontrados =
                new ArrayList<>(pokemonNaoEncontrados);
        relatorio.nomesMovimentosNaoEncontrados =
                new ArrayList<>(movimentosNaoEncontrados);

        return relatorio;
    }

    private Optional<Move> buscarMovimento(String nomeInterno) {
        String nome = nomeInterno.toLowerCase(Locale.ROOT);

        if (nome.equals("psychic")) {
            nome = "psychic_m";
        }

        return moveRepository.findByName(nome);
    }

    private String normalizarNomePokemon(String nomeInterno) {
        String nome = nomeInterno
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);

        // Nomes especiais
        if (nome.equals("farfetch_d")) {
            nome = "farfetch_d_plain";
        } else if (nome.equals("mr__mime")) {
            nome = "mr__mime_plain";
        } else if (nome.equals("mr__rime")) {
            // No banco, mr__rime não possui o sufixo _plain.
            nome = "mr__rime";
        } else if (nome.equals("mime_jr")) {
            nome = "mime_jr_";
        } else if (nome.equals("mewtwo")) {
            nome = "mewtwo_plain";
        } else {
            String[] formasAlternativas = {
                "arcanine", "articuno", "corsola", "diglett",
                "dugtrio", "electrode", "exeggutor", "geodude",
                "golem", "graveler", "grimer", "growlithe",
                "marowak", "meowth", "moltres", "muk",
                "ninetales", "persian", "ponyta", "qwilfish",
                "raichu", "rapidash", "raticate", "rattata",
                "sandshrew", "sandslash", "slowbro", "slowking",
                "slowpoke", "sneasel", "tauros", "typhlosion",
                "ursaluna", "voltorb", "vulpix", "weezing",
                "wooper", "zapdos"
            };

            for (String base : formasAlternativas) {
                if (nome.equals(base)) {
                    nome = base + "_plain";
                    break;
                }
            }
        }

        return nome;
    }

    public static class ImportReport {

        private int totalEncontrado;
        private int importados;
        private int pokemonNaoEncontrados;
        private int movimentosNaoEncontrados;
        private int duplicados;
        private List<String> nomesPokemonNaoEncontrados = new ArrayList<>();
        private List<String> nomesMovimentosNaoEncontrados = new ArrayList<>();

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

        public List<String> getNomesPokemonNaoEncontrados() {
            return nomesPokemonNaoEncontrados;
        }

        public List<String> getNomesMovimentosNaoEncontrados() {
            return nomesMovimentosNaoEncontrados;
        }
    }
}
