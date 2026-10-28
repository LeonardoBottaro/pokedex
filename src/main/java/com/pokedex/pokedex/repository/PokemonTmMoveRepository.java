
package com.pokedex.pokedex.repository;

import com.pokedex.pokedex.model.PokemonTmMove;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PokemonTmMoveRepository
        extends JpaRepository<PokemonTmMove, Integer> {

    boolean existsByPokemon_IdAndMove_Id(
            Integer pokemonId, Integer moveId);

    List<PokemonTmMove> findByPokemon_NameIgnoreCaseOrderByMove_NameAsc(
            String pokemonName);
}
