
package com.pokedex.pokedex.dto;

import java.util.List;

public record EvolutionNodeResponse(
        String pokemon,
        List<EvolutionConditionResponse> condicoes,
        List<EvolutionNodeResponse> evolucoesPosteriores
) {
}
