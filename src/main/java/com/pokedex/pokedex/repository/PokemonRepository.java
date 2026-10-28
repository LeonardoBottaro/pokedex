package com.pokedex.pokedex.repository;

import com.pokedex.pokedex.model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {

    // Busca pelo nome exato, diferenciando maiúsculas/minúsculas conforme o banco.
    Optional<Pokemon> findByName(String name);

    // Busca pelo nome exato, ignorando maiúsculas/minúsculas.
    Optional<Pokemon> findByNameIgnoreCase(String name);

    // Busca por parte do nome, ignorando maiúsculas/minúsculas.
    List<Pokemon> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}
