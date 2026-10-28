
package com.pokedex.pokedex.dto;

public record EvolutionResponse(
        String fromPokemon,
        String toPokemon,
        String method,
        String conditionValue
) {
}
