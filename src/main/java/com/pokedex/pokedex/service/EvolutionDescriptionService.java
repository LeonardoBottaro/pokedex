
package com.pokedex.pokedex.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EvolutionDescriptionService {

    private static final Map<String, String> ITENS = Map.ofEntries(
            Map.entry("FIRE_STONE", "Pedra de Fogo"),
            Map.entry("WATER_STONE", "Pedra de Água"),
            Map.entry("THUNDERSTONE", "Pedra do Trovão"),
            Map.entry("LEAF_STONE", "Pedra da Folha"),
            Map.entry("MOON_STONE", "Pedra da Lua"),
            Map.entry("SUN_STONE", "Pedra do Sol"),
            Map.entry("ICE_STONE", "Pedra de Gelo"),
            Map.entry("SHINY_STONE", "Pedra Brilhante")
    );

    private static final Map<String, String> LOCAIS = Map.of(
            "ICE_PATH", "Caminho de Gelo",
            "ILEX_FOREST", "Floresta de Ilex"
    );

    public String descrever(String metodo, String valor) {
        if (metodo == null) {
            return "Condição de evolução não especificada.";
        }

        String condicao = valor == null ? "" : valor.trim();

        return switch (metodo) {
            case "EVOLVE_LEVEL" ->
                    descreverNivel(condicao);

            case "EVOLVE_ITEM" ->
                    descreverItem(condicao);

            case "EVOLVE_HAPPINESS" ->
                    descreverFelicidade(condicao);

            case "EVOLVE_LOCATION" ->
                    descreverLocal(condicao);

            case "EVOLVE_TRADE" ->
                    "Troque este Pokémon.";

            case "EVOLVE_HOLDING" ->
                    "Troque este Pokémon enquanto segura "
                            + nomeItem(condicao) + ".";

            case "EVOLVE_STAT" ->
                    "Cumpra a condição de atributos: "
                            + condicao + ".";

            case "EVOLVE_CRIT" ->
                    "Cumpra a condição de acertos críticos: "
                            + condicao + ".";

            case "EVOLVE_PARTY" ->
                    "Cumpra a condição relacionada ao grupo: "
                            + condicao + ".";

            default ->
                    "Condição de evolução: " + metodo
                            + (condicao.isBlank()
                            ? ""
                            : " (" + condicao + ")")
                            + ".";
        };
    }

    private String descreverNivel(String valor) {
        if (valor.matches("\\d+")) {
            return "Alcance o nível " + valor + ".";
        }

        return "Evolua ao cumprir a condição de nível: "
                + valor + ".";
    }

    private String descreverItem(String valor) {
        if (valor.isBlank()) {
            return "Use o item de evolução necessário.";
        }

        String nome = ITENS.get(valor);

        if (nome == null) {
            return "Use o item de evolução: " + valor + ".";
        }

        return "Use uma " + nome + ".";
    }

    private String descreverFelicidade(String valor) {
        return switch (valor) {
            case "TR_MORNDAY" ->
                    "Aumente a felicidade durante o dia.";

            case "TR_EVENITE" ->
                    "Aumente a felicidade durante a noite.";

            default ->
                    "Aumente a felicidade do Pokémon"
                            + (valor.isBlank()
                            ? "."
                            : " (" + valor + ").");
        };
    }

    private String descreverLocal(String valor) {
        String local = LOCAIS.get(valor);

        if (local != null) {
            return "Evolua no local: " + local + ".";
        }

        return "Evolua na localização especial: "
                + (valor.isBlank() ? "não especificada" : valor)
                + ".";
    }

    private String nomeItem(String valor) {
        return ITENS.getOrDefault(valor, valor);
    }
}
