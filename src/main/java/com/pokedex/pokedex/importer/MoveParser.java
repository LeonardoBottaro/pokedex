package com.pokedex.pokedex.importer;

// Importa a classe que converte objetos Java para JSON.
import com.fasterxml.jackson.databind.ObjectMapper;

// Importa as classes necessárias para ler e gravar arquivos.
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

// Importa as classes usadas para armazenar e processar os movimentos.
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Declara o importador responsável por ler os movimentos do arquivo ASM.
public class MoveParser {

    // Define o padrão de uma linha que declara um movimento no arquivo ASM.
    // Os grupos representam: nome, efeito, poder, tipo, precisão, PP,
    // chance de efeito e categoria.
    private static final Pattern PADRAO_MOVIMENTO = Pattern.compile(
            "^move\\s+([A-Z0-9_]+)\\s*,\\s*" +
                    "([A-Z0-9_]+)\\s*,\\s*" +
                    "(-?\\d+)\\s*,\\s*" +
                    "([A-Z0-9_]+)\\s*,\\s*" +
                    "(-?\\d+)\\s*,\\s*" +
                    "(\\d+)\\s*,\\s*" +
                    "(\\d+)\\s*,\\s*" +
                    "([A-Z0-9_]+)\\s*$");

    // Representa os dados extraídos de um único movimento.
    // Os nomes dos campos serão usados como nomes das propriedades no JSON.
    public record Move(
            String name,
            String effect,
            int power,
            String type,
            int accuracy,
            int pp,
            int effectChance,
            String category) {
    }

    // Inicia a execução do importador.
    public static void main(String[] args) throws IOException {

        // Localiza o arquivo de origem e define onde o JSON será salvo.
        Path arquivoEntrada = localizarArquivoEntrada();
        Path arquivoSaida = Path.of("moves.json");

        // Lê os movimentos selecionando exclusivamente o ramo FAITHFUL.
        List<Move> movimentos = analisarArquivo(arquivoEntrada);

        // Cria o conversor JSON e configura a saída para facilitar a leitura.
        ObjectMapper objectMapper = new ObjectMapper();

        // Grava a lista de movimentos em UTF-8, com indentação e quebras de linha.
        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(arquivoSaida.toFile(), movimentos);

        // Exibe o resultado da execução no terminal.
        System.out.println("Arquivo ASM lido: " + arquivoEntrada.toAbsolutePath());
        System.out.println("Movimentos encontrados: " + movimentos.size());
        System.out.println("Arquivo JSON gerado: " + arquivoSaida.toAbsolutePath());
    }

    // Procura o arquivo ASM usando um caminho informado ou os locais padrão.
    private static Path localizarArquivoEntrada() throws IOException {

        // Permite informar o caminho completo do arquivo ao executar o programa.
        String caminhoConfigurado = System.getProperty("moves.asm.path");

        // Utiliza o caminho informado quando a propriedade foi definida.
        if (caminhoConfigurado != null && !caminhoConfigurado.isBlank()) {
            Path caminhoInformado = Path.of(caminhoConfigurado);

            // Retorna o caminho caso ele corresponda a um arquivo existente.
            if (Files.isRegularFile(caminhoInformado)) {
                return caminhoInformado;
            }

            // Informa o caminho recebido quando o arquivo não existe.
            throw new IOException(
                    "O arquivo informado não existe: " + caminhoInformado);
        }

        // Verifica os caminhos relativos mais comuns do projeto.
        Path[] caminhosPossiveis = {
                Path.of("data", "moves", "moves.asm"),
                Path.of("moves.asm"),
                Path.of("polishedcrystal", "data", "moves", "moves.asm")
        };

        // Retorna o primeiro caminho que corresponder a um arquivo existente.
        for (Path caminho : caminhosPossiveis) {
            if (Files.isRegularFile(caminho)) {
                return caminho;
            }
        }

        // Informa como resolver o problema caso nenhum caminho seja válido.
        throw new IOException(
                "Não foi possível encontrar moves.asm. " +
                        "Informe o caminho completo usando a propriedade moves.asm.path.");
    }

    // Lê o arquivo ASM e extrai os movimentos da tabela principal.
    private static List<Move> analisarArquivo(Path arquivo) throws IOException {

        // Carrega todas as linhas do arquivo usando a codificação UTF-8.
        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);

        // Armazena os movimentos encontrados na ordem em que aparecem no arquivo.
        List<Move> movimentos = new ArrayList<>();

        // Indica se o parser já chegou ao início da tabela de movimentos.
        boolean dentroDaTabela = false;

        // Indica se o parser está dentro de um bloco condicional FAITHFUL.
        boolean dentroDoBlocoFaithful = false;

        // Controla se o ramo atual do bloco FAITHFUL deve ser processado.
        boolean incluirRamoAtual = true;

        // Percorre todas as linhas do arquivo de origem.
        for (String linhaOriginal : linhas) {

            // Remove espaços externos para facilitar o reconhecimento das instruções.
            String linha = linhaOriginal.trim();

            // Ignora linhas vazias e comentários que ocupam uma linha inteira.
            if (linha.isEmpty() || linha.startsWith(";")) {
                continue;
            }

            // Detecta o início da tabela de movimentos e ignora o conteúdo anterior.
            if (!dentroDaTabela) {
                if (linha.equals("Moves::")) {
                    dentroDaTabela = true;
                }

                // Continua até encontrar o início da tabela.
                continue;
            }

            // Encerra a leitura quando a tabela principal chega ao seu marcador final.
            if (linha.startsWith("assert_table_length NUM_ATTACKS")) {
                break;
            }

            // Reconhece o início de um bloco específico da variante FAITHFUL.
            if (linha.equals("if DEF(FAITHFUL)")) {
                dentroDoBlocoFaithful = true;

                // O ramo inicial do bloco é o ramo FAITHFUL que queremos importar.
                incluirRamoAtual = true;
                continue;
            }

            // Reconhece o ramo alternativo de um bloco FAITHFUL.
            if (linha.equals("else") && dentroDoBlocoFaithful) {

                // Desativa a leitura do ramo alternativo, que não será importado.
                incluirRamoAtual = false;
                continue;
            }

            // Reconhece o encerramento do bloco condicional.
            if (linha.equals("endc") && dentroDoBlocoFaithful) {

                // Restaura o estado padrão para as linhas posteriores ao bloco.
                dentroDoBlocoFaithful = false;
                incluirRamoAtual = true;
                continue;
            }

            // Ignora as linhas do ramo else dos blocos FAITHFUL.
            if (dentroDoBlocoFaithful && !incluirRamoAtual) {
                continue;
            }

            // Remove comentários que estejam no final de uma linha de código.
            int indiceComentario = linha.indexOf(';');

            // Mantém somente o código anterior ao comentário, quando houver.
            if (indiceComentario >= 0) {
                linha = linha.substring(0, indiceComentario).trim();
            }

            // Ignora linhas que não declaram um movimento.
            if (!linha.startsWith("move ")) {
                continue;
            }

            // Compara a declaração com o padrão esperado para um movimento.
            Matcher matcher = PADRAO_MOVIMENTO.matcher(linha);

            // Interrompe a execução caso uma declaração não siga o formato esperado.
            if (!matcher.matches()) {
                throw new IOException(
                        "Declaração de movimento inválida: " + linhaOriginal);
            }

            // Converte os campos numéricos extraídos do arquivo para inteiros.
            int poder = Integer.parseInt(matcher.group(3));
            int precisao = Integer.parseInt(matcher.group(5));
            int pp = Integer.parseInt(matcher.group(6));
            int chanceEfeito = Integer.parseInt(matcher.group(7));

            // Cria o objeto com os dados do movimento e adiciona-o à lista.
            Move movimento = new Move(
                    matcher.group(1),
                    matcher.group(2),
                    poder,
                    matcher.group(4),
                    precisao,
                    pp,
                    chanceEfeito,
                    matcher.group(8));

            // Mantém os movimentos na mesma ordem da tabela original.
            movimentos.add(movimento);
        }

        // Retorna todos os movimentos encontrados no ramo FAITHFUL.
        return movimentos;
    }
}
