package com.pokedex.pokedex.importer; // Define o pacote onde esta classe está localizada.

import com.fasterxml.jackson.databind.ObjectMapper; //converte objetos Java em JSON.
import com.fasterxml.jackson.databind.SerializationFeature; //permite configurar a formatação do JSON.

//guardam os Pokémon processados antes da exportação.
import java.util.ArrayList;
import java.util.List;

// Permite armazenar associações entre nomes e números.
import java.util.HashMap;
import java.util.Map;

// Importa a classe que permite ler e manipular arquivos e diretórios.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Importa as classes usadas para procurar padrões de texto com expressões regulares.
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Declara a classe responsável por interpretar os arquivos .asm dos Pokémon.
public class AsmPokemonParser {

    /**
     * Lê a ordem dos Pokémon definida em dex_order_new.asm.
     *
     * Cada linha válida no formato "dp NOME_DO_POKEMON" recebe
     * um número sequencial, começando em 1.
     *
     * @param arquivo caminho do arquivo dex_order_new.asm
     * @return mapa que associa o nome do Pokémon ao número da Pokédex
     * @throws IOException se ocorrer um erro ao ler o arquivo
     */
    private static Map<String, Integer> lerOrdemJohto(
            Path arquivo) throws IOException {

        // Guarda o nome de cada Pokémon e sua posição na tabela.
        Map<String, Integer> numeros = new HashMap<>();

        // Reconhece linhas com "dp" seguido do identificador do Pokémon.
        Pattern padrao = Pattern.compile(
                "^\\s*dp\\s+([A-Z0-9_]+)\\s*$");

        // O primeiro Pokémon válido receberá o número 1.
        int numero = 0;

        // Lê o arquivo linha por linha.
        for (String linha : Files.readAllLines(arquivo)) {

            // Verifica se a linha corresponde ao formato esperado.
            Matcher matcher = padrao.matcher(linha);

            // Ignora comentários, cabeçalhos e outras linhas.
            if (matcher.matches()) {

                // Avança a numeração para a próxima entrada.
                numero++;

                // Obtém o identificador e converte para letras minúsculas.
                String nome = matcher.group(1).toLowerCase();

                // Registra a posição do Pokémon no mapa.
                numeros.put(nome, numero);
            }
        }

        // Retorna o mapa com os números encontrados.
        return numeros;
    }

    // Cria um padrão para identificar a linha que contém os atributos-base.
    // Espera uma linha no formato:
    // bst TOTAL, HP, ATAQUE, DEFESA, VELOCIDADE, ATAQUE_ESPECIAL, DEFESA_ESPECIAL
    //
    // \\s* permite espaços opcionais.
    // (\\d+) captura um ou mais números.
    // \\s*,\\s* permite uma vírgula com espaços opcionais ao redor.
    //
    // O ^ indica o início da linha e o $ indica o fim da linha.
    private static final Pattern STATS_PATTERN = Pattern.compile(
            "^\\s*bst\\s+(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*$");

    // Cria um padrão para encontrar a linha dos tipos do Pokémon.
    // Espera uma linha semelhante a:
    // db FAIRY, FLYING ; type
    //
    // O primeiro grupo captura o primeiro tipo.
    // O segundo grupo captura o segundo tipo, caso exista.
    // [A-Z0-9_]+ aceita letras maiúsculas, números e underline.
    private static final Pattern TYPE_PATTERN = Pattern.compile(
            "^\\s*db\\s+([A-Z0-9_]+)(?:\\s*,\\s*([A-Z0-9_]+))?\\s*;\\s*type\\s*$");

    // Cria um padrão para encontrar os itens que o Pokémon pode carregar.
    // Espera uma linha semelhante a:
    // db NO_ITEM, NO_ITEM ; held items
    //
    // O primeiro grupo captura o primeiro item.
    // O segundo grupo captura o segundo item.
    private static final Pattern ITEMS_PATTERN = Pattern.compile(
            "^\\s*db\\s+([A-Z0-9_]+)\\s*,\\s*([A-Z0-9_]+)\\s*;\\s*held items\\s*$");

    // Define uma estrutura para guardar os dados extraídos de um Pokémon.
    // "record" é uma funcionalidade do Java que cria uma classe simples
    // para armazenar dados, com construtor, acessores e outros métodos.
    //
    // Os tipos String guardam texto; os tipos int guardam números inteiros.
    public record PokemonData(
            String name, // Nome inicial obtido do nome do arquivo.
            int total, // Soma dos seis atributos-base.
            int hp, // Pontos de vida.
            int attack, // Ataque físico.
            int defense, // Defesa física.
            int speed, // Velocidade.
            int specialAttack, // Ataque especial.
            int specialDefense, // Defesa especial.
            String type1, // Primeiro tipo do Pokémon.
            String type2, // Segundo tipo, se existir.
            String item1, // Primeiro item possível.
            String item2, // Segundo item possível.
            int johtoDexNumber // Número do Pokémon na ordem da Pokédex de Johto.
    ) {
    } // O corpo do record pode ficar vazio porque o Java gera os métodos
      // automaticamente.

    // Declara o método responsável por ler e interpretar um arquivo.
    // "static" permite chamar o método sem criar um objeto da classe.
    // "Path file" é o caminho do arquivo que será lido.
    // "throws IOException" informa que problemas de leitura podem ser repassados
    // para quem chamou o método.
    public static PokemonData parse(Path file) throws IOException {

        // Obtém o nome do arquivo, por exemplo "togetic.asm".
        // Depois remove a extensão ".asm", resultando em "togetic".
        String name = file.getFileName()
                .toString()
                .replace(".asm", "");

        // Inicializa o total como null porque ainda não encontramos o valor.
        // Integer, ao contrário de int, pode representar a ausência de valor com null.
        Integer total = null;

        // Inicializa o vetor de atributos como null.
        // Quando encontrarmos a linha "bst", colocaremos os seis atributos nele.
        int[] stats = null;

        // Inicializa os tipos como null para indicar que ainda não foram encontrados.
        String type1 = null;
        String type2 = null;

        // Inicializa os itens como null pelo mesmo motivo.
        String item1 = null;
        String item2 = null;

        // Lê todas as linhas do arquivo e percorre cada uma delas.
        // "line" representa o texto da linha atual.
        for (String line : Files.readAllLines(file)) {

            // Verifica se a linha atual corresponde ao formato dos atributos-base.
            // matcher cria um objeto que permite testar e extrair os grupos capturados.
            Matcher statsMatcher = STATS_PATTERN.matcher(line);

            // Verifica se a linha corresponde ao formato dos tipos.
            Matcher typeMatcher = TYPE_PATTERN.matcher(line);

            // Verifica se a linha corresponde ao formato dos itens.
            Matcher itemsMatcher = ITEMS_PATTERN.matcher(line);

            // Se a linha inteira corresponder ao padrão de atributos...
            if (statsMatcher.matches()) {

                // Converte o primeiro grupo capturado para inteiro.
                // Esse grupo representa o total declarado na linha "bst".
                total = Integer.parseInt(statsMatcher.group(1));

                // Cria um vetor com os seis atributos-base.
                // Cada group() corresponde a um grupo capturado pela expressão regular.
                stats = new int[] {
                        Integer.parseInt(statsMatcher.group(2)), // Posição 0: HP.
                        Integer.parseInt(statsMatcher.group(3)), // Posição 1: Ataque.
                        Integer.parseInt(statsMatcher.group(4)), // Posição 2: Defesa.
                        Integer.parseInt(statsMatcher.group(5)), // Posição 3: Velocidade.
                        Integer.parseInt(statsMatcher.group(6)), // Posição 4: Ataque especial.
                        Integer.parseInt(statsMatcher.group(7)) // Posição 5: Defesa especial.
                };
            }

            // Se a linha corresponder ao padrão de tipos...
            if (typeMatcher.matches()) {

                // Guarda o primeiro tipo.
                type1 = typeMatcher.group(1);

                // Guarda o segundo tipo.
                // Se o segundo tipo for opcional e não estiver presente, o valor será null.
                type2 = typeMatcher.group(2);
            }
            if (type1 != null && type1.equals(type2)) {
                type2 = null;
            }

            // Se a linha corresponder ao padrão dos itens...
            if (itemsMatcher.matches()) {

                // Guarda o identificador do primeiro item.
                item1 = itemsMatcher.group(1);

                // Guarda o identificador do segundo item.
                item2 = itemsMatcher.group(2);
            }
        } // Termina o loop: todas as linhas do arquivo foram verificadas.

        // Verifica se os campos obrigatórios foram encontrados.
        // Se algum estiver ausente, interrompe o processamento com uma mensagem de
        // erro.
        if (total == null || stats == null
                || type1 == null || item1 == null) {

            // Lança uma exceção informando qual arquivo não pôde ser interpretado.
            throw new IllegalArgumentException(
                    "Não foi possível encontrar todos os campos esperados em: "
                            + file);
        }

        // Inicializa a variável que receberá a soma calculada dos atributos.
        int calculatedTotal = 0;

        // Percorre todos os valores do vetor stats.
        for (int stat : stats) {

            // Soma o atributo atual ao total calculado.
            calculatedTotal += stat;
        }

        // Compara a soma calculada com o total declarado no arquivo.
        if (calculatedTotal != total) {

            // Se os valores forem diferentes, informa o erro.
            // Isso ajuda a detectar dados incorretos ou uma leitura errada.
            throw new IllegalArgumentException(
                    "Total inválido para " + name
                            + ": declarado=" + total
                            + ", calculado=" + calculatedTotal);
        }

        // Cria e retorna o objeto com os dados extraídos do arquivo.
        // O número da Johto Dex será preenchido posteriormente no main().
        return new PokemonData(
                name, // Nome do Pokémon.
                total, // Soma dos atributos-base.
                stats[0], // HP.
                stats[1], // Ataque.
                stats[2], // Defesa.
                stats[3], // Velocidade.
                stats[4], // Ataque especial.
                stats[5], // Defesa especial.
                type1, // Primeiro tipo.
                type2, // Segundo tipo, se existir.
                item1, // Primeiro item possível.
                item2, // Segundo item possível.
                0 // Valor temporário até o main() definir o número.
        );
    } // Termina o método parse().

    // Este é o ponto de entrada do programa quando ele é executado diretamente.
    // O Java começa a execução por este método main().

    /**
     * Processa os arquivos dos Pokémon e exporta os dados para JSON.
     *
     * Além dos atributos básicos, associa cada Pokémon ao número
     * encontrado na tabela dex_order_new.asm.
     */
    public static void main(String[] args) throws IOException {

        // Define a pasta com os arquivos .asm dos Pokémon.
        Path pasta = Path.of(
                "polishedcrystal",
                "data", "pokemon", "base_stats");

        // Define o caminho do arquivo que receberá os dados em JSON.
        Path arquivoJson = Path.of("pokemon.json");

        // Define o caminho do arquivo que contém a ordem da Pokédex.
        Path arquivoOrdemJohto = Path.of(
                "polishedcrystal",
                "data", "pokemon", "dex_order_new.asm");

        // Lê a tabela e associa cada Pokémon ao seu número.
        Map<String, Integer> numerosJohto = lerOrdemJohto(arquivoOrdemJohto);

        // Cria a lista que armazenará os Pokémon processados.
        List<PokemonData> pokemons = new ArrayList<>();

        // Conta quantos arquivos apresentaram erros.
        int erros = 0;

        // Lista os arquivos existentes na pasta de atributos básicos.
        try (var arquivos = Files.list(pasta)) {

            // Mantém apenas arquivos .asm e ordena seus caminhos.
            var listaArquivos = arquivos
                    .filter(Files::isRegularFile)
                    .filter(caminho -> caminho.toString().endsWith(".asm"))
                    .sorted()
                    .toList();

            // Processa cada arquivo de Pokémon.
            for (Path caminho : listaArquivos) {

                try {
                    // Extrai os dados básicos usando o parser existente.
                    PokemonData pokemon = parse(caminho);

                    // Procura o número de Johto pelo nome do Pokémon.
                    Integer numeroJohto = numerosJohto.get(
                            pokemon.name().toLowerCase());

                    // Interrompe o processamento desse arquivo se o nome
                    // não existir na tabela, evitando gerar um número falso.
                    if (numeroJohto == null) {
                        throw new IllegalArgumentException(
                                "Pokémon não encontrado em dex_order_new.asm: "
                                        + pokemon.name());
                    }

                    // Cria um novo record com os dados originais
                    // e acrescenta o número da Pokédex de Johto.
                    PokemonData pokemonComNumero = new PokemonData(
                            pokemon.name(), // Nome do Pokémon.
                            pokemon.total(), // Total dos atributos.
                            pokemon.hp(), // Pontos de vida.
                            pokemon.attack(), // Ataque.
                            pokemon.defense(), // Defesa.
                            pokemon.speed(), // Velocidade.
                            pokemon.specialAttack(), // Ataque especial.
                            pokemon.specialDefense(), // Defesa especial.
                            pokemon.type1(), // Primeiro tipo.
                            pokemon.type2(), // Segundo tipo.
                            pokemon.item1(), // Primeiro item.
                            pokemon.item2(), // Segundo item.
                            numeroJohto // Número da Johto Dex.
                    );

                    // Adiciona o Pokémon completo à lista de exportação.
                    pokemons.add(pokemonComNumero);

                } catch (IOException | IllegalArgumentException e) {

                    // Informa o erro e continua com os outros arquivos.
                    System.err.println(
                            "Erro em " + caminho + ": " + e.getMessage());

                    // Atualiza a quantidade de arquivos com erro.
                    erros++;
                }
            }
        }

        // Cria o conversor responsável por gerar o JSON.
        ObjectMapper mapper = new ObjectMapper();

        // Ativa a indentação para facilitar a leitura do arquivo.
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Escreve todos os Pokémon processados no arquivo JSON.
        mapper.writeValue(arquivoJson.toFile(), pokemons);

        // Exibe o resumo final do processamento.
        System.out.println("\n===== RESUMO DA EXPORTAÇÃO =====");
        System.out.println("Pokémon exportados: " + pokemons.size());
        System.out.println("Arquivos com erro: " + erros);
        System.out.println(
                "Arquivo gerado em: " + arquivoJson.toAbsolutePath());
    }

} // Termina a classe AsmPokemonParser.
