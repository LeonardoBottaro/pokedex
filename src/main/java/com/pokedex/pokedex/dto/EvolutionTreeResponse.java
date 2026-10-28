
package com.pokedex.pokedex.dto;

import java.util.List;

public record EvolutionTreeResponse(
        String pokemon,
        List<EvolutionResponse> evolucoesAnteriores,
        List<EvolutionResponse> evolucoesPosteriores,
        EvolutionNodeResponse arvoreEvolutiva
) {
}
