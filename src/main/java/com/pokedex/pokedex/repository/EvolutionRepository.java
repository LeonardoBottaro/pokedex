
package com.pokedex.pokedex.repository;

import com.pokedex.pokedex.model.Evolution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvolutionRepository extends JpaRepository<Evolution, Integer> {

    List<Evolution> findByFromPokemon_NameIgnoreCaseOrderByToPokemon_NameAsc(
        String pokemonName
    );

    List<Evolution> findByFromPokemon_NameIgnoreCaseOrToPokemon_NameIgnoreCase(
        String fromName,
        String toName
    );

    boolean existsByFromPokemon_IdAndToPokemon_IdAndMethodAndConditionValue(
        Integer fromPokemonId,
        Integer toPokemonId,
        String method,
        String conditionValue
    );

    List<Evolution> findByToPokemon_NameIgnoreCaseOrderByFromPokemon_NameAsc(
            String pokemonName
    );  

}
