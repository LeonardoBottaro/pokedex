package com.pokedex.pokedex.service;

// Importa o parser responsável por ler o arquivo ASM.
import com.pokedex.pokedex.importer.PokemonLevelUpMoveParser;
import com.pokedex.pokedex.importer.PokemonLevelUpMoveParser.LevelUpMoveData;

// Importa as entidades do banco de dados.
import com.pokedex.pokedex.model.Move;
import com.pokedex.pokedex.model.Pokemon;
import com.pokedex.pokedex.model.PokemonLevelUpMove;

// Importa os repositórios usados para consultar e salvar os dados.
import com.pokedex.pokedex.repository.MoveRepository;
import com.pokedex.pokedex.repository.PokemonLevelUpMoveRepository;
import com.pokedex.pokedex.repository.PokemonRepository;

// Importa recursos do Spring.
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importa recursos para leitura de arquivos.
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// Serviço responsável pela importação dos movimentos aprendidos por nível.
@Service
public class PokemonLevelUpMoveImportService {

    // Repositórios utilizados para consultar e salvar os dados.
    private final PokemonRepository pokemonRepository;
    private final MoveRepository moveRepository;
    private final PokemonLevelUpMoveRepository levelUpMoveRepository;

    // Guarda os nomes internos que precisam de uma conversão especial.
    private static final Map<String, String> NOMES_ESPECIAIS = criarMapaNomes();

    // Recebe as dependências gerenciadas pelo Spring.
    public PokemonLevelUpMoveImportService(
            PokemonRepository pokemonRepository,
            MoveRepository moveRepository,
            PokemonLevelUpMoveRepository levelUpMoveRepository) {

        this.pokemonRepository = pokemonRepository;
        this.moveRepository = moveRepository;
        this.levelUpMoveRepository = levelUpMoveRepository;
    }

    // Cria as conversões entre nomes internos do ASM e nomes do banco.
    private static Map<String, String> criarMapaNomes() {
        Map<String, String> nomes = new LinkedHashMap<>();

        // Corrige nomes que usam letras maiúsculas no meio da palavra.
        nomes.put("HoOh", "ho_oh");
        nomes.put("NidoranF", "nidoran_f");
        nomes.put("NidoranM", "nidoran_m");
        nomes.put("MimeJr", "mime_jr_");
        nomes.put("PorygonZ", "porygon_z");
        nomes.put("SirfetchD", "sirfetch_d");
        nomes.put("MrMime", "mr_mime");
        nomes.put("MrRime", "mr__rime");

        return Map.copyOf(nomes);
    }

    // Converte o nome interno para o padrão utilizado no banco.
    private String normalizarNomePokemon(String nomeInterno) {

        // Usa a conversão especial quando ela estiver cadastrada.
        String nomeEspecial = NOMES_ESPECIAIS.get(nomeInterno);
        if (nomeEspecial != null) {
            return nomeEspecial;
        }

        // Converte para minúsculas e aplica o padrão de nomes do banco.
        return nomeInterno.toLowerCase(Locale.ROOT);
    }

    // Procura um Pokémon usando o nome interno do arquivo ASM.
    private Pokemon buscarPokemon(String nomeInterno) {
        String nomeBanco = normalizarNomePokemon(nomeInterno);

        // Tenta primeiro o nome normalizado.
        Pokemon pokemon = pokemonRepository.findByName(nomeBanco)
                .orElse(null);

        if (pokemon != null) {
            return pokemon;
        }

        // Não remove sufixos de formas regionais automaticamente.
        // Isso evita associar movimentos à forma errada.
        return null;
    }

    // Importa os movimentos do arquivo ASM e retorna um relatório.
    @Transactional
    public ImportReport importarMovimentosPorNivel(Path caminhoArquivo)
            throws IOException {

        // Lê o arquivo e extrai os movimentos aprendidos por nível.
        PokemonLevelUpMoveParser parser = new PokemonLevelUpMoveParser();
        List<LevelUpMoveData> dados = parser.parse(caminhoArquivo);

        // Inicializa os contadores da importação.
        int importados = 0;
        int pokemonNaoEncontrados = 0;
        int movimentosNaoEncontrados = 0;
        int duplicados = 0;

        // Guarda os nomes únicos que não existem no banco.
        Set<String> nomesPokemonNaoEncontrados = new LinkedHashSet<>();

        // Percorre os registros encontrados no arquivo.
        for (LevelUpMoveData dado : dados) {

            // Procura o Pokémon usando a normalização de nomes.
            Pokemon pokemon = buscarPokemon(dado.getPokemonName());

            // Ignora o registro se o Pokémon não existir no banco.
            if (pokemon == null) {
                pokemonNaoEncontrados++;
                nomesPokemonNaoEncontrados.add(dado.getPokemonName());
                continue;
            }

            // Procura o movimento pelo nome interno.
            Move move = moveRepository.findByName(dado.getMoveName())
                    .orElse(null);

            // Ignora o registro se o movimento não existir no banco.
            if (move == null) {
                movimentosNaoEncontrados++;
                continue;
            }

            // Verifica se o registro já existe para evitar duplicações.
            boolean jaExiste =
                    levelUpMoveRepository.existsByPokemonIdAndMoveIdAndLevel(
                            pokemon.getId(),
                            move.getId(),
                            dado.getLevel()
                    );

            // Conta os registros que já estão salvos.
            if (jaExiste) {
                duplicados++;
                continue;
            }

            // Cria o relacionamento entre Pokémon, movimento e nível.
            PokemonLevelUpMove registro = new PokemonLevelUpMove();
            registro.setPokemon(pokemon);
            registro.setMove(move);
            registro.setLevel(dado.getLevel());

            // Salva o novo registro no banco.
            levelUpMoveRepository.save(registro);
            importados++;
        }

        // Retorna o resumo da importação.
        return new ImportReport(
                dados.size(),
                importados,
                pokemonNaoEncontrados,
                movimentosNaoEncontrados,
                duplicados,
                nomesPokemonNaoEncontrados
        );
    }

    // Analisa o arquivo sem inserir ou modificar registros no banco.
    @Transactional(readOnly = true)
    public ImportReport diagnosticarMovimentosPorNivel(Path caminhoArquivo)
            throws IOException {

        // Lê o arquivo e extrai os movimentos aprendidos por nível.
        PokemonLevelUpMoveParser parser = new PokemonLevelUpMoveParser();
        List<LevelUpMoveData> dados = parser.parse(caminhoArquivo);

        // Inicializa os contadores do diagnóstico.
        int pokemonNaoEncontrados = 0;
        int movimentosNaoEncontrados = 0;
        int duplicados = 0;
        int registrosValidos = 0;

        // Armazena os nomes únicos dos Pokémon ausentes.
        Set<String> nomesPokemonNaoEncontrados = new LinkedHashSet<>();

        // Percorre os registros extraídos do arquivo.
        for (LevelUpMoveData dado : dados) {

            // Procura o Pokémon usando a normalização de nomes.
            Pokemon pokemon = buscarPokemon(dado.getPokemonName());

            // Registra os Pokémon que não foram encontrados.
            if (pokemon == null) {
                pokemonNaoEncontrados++;
                nomesPokemonNaoEncontrados.add(dado.getPokemonName());
                continue;
            }

            // Procura o movimento pelo nome interno.
            Move move = moveRepository.findByName(dado.getMoveName())
                    .orElse(null);

            // Registra os movimentos que não foram encontrados.
            if (move == null) {
                movimentosNaoEncontrados++;
                continue;
            }

            // Verifica se o relacionamento já existe no banco.
            boolean jaExiste =
                    levelUpMoveRepository.existsByPokemonIdAndMoveIdAndLevel(
                            pokemon.getId(),
                            move.getId(),
                            dado.getLevel()
                    );

            // Conta os registros que já existem.
            if (jaExiste) {
                duplicados++;
                continue;
            }

            // Conta os registros que poderiam ser importados.
            registrosValidos++;
        }

        // Retorna o relatório sem modificar os dados do banco.
        return new ImportReport(
                dados.size(),
                registrosValidos,
                pokemonNaoEncontrados,
                movimentosNaoEncontrados,
                duplicados,
                nomesPokemonNaoEncontrados
        );
    }

    // Representa o resultado de uma importação ou de um diagnóstico.
    public static class ImportReport {

        private final int totalEncontrado;
        private final int importados;
        private final int pokemonNaoEncontrados;
        private final int movimentosNaoEncontrados;
        private final int duplicados;
        private final Set<String> nomesPokemonNaoEncontrados;

        // Inicializa todos os campos do relatório.
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
