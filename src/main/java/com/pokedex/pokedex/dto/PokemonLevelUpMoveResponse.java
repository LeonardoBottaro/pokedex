package com.pokedex.pokedex.dto;

public record PokemonLevelUpMoveResponse(
        Integer level,
        MoveDetails move
) {
    public record MoveDetails(
            String name,
            String type,
            Integer power,
            Integer accuracy,
            Integer pp,
            String category,
            String effect,
            Integer effectChance
    ) {
    }
}
