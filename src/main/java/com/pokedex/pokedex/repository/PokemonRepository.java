package com.pokedex.pokedex.repository;

// Importa a entidade persistida no banco.
import com.pokedex.pokedex.model.Pokemon;

// Importa a interface de persistência do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa o tipo usado quando um resultado pode não existir.
import java.util.Optional;

// Define as operações de acesso à tabela pokemon.
public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {

    // Busca um Pokémon pelo nome, retornando um resultado opcional.
    Optional<Pokemon> findByName(String name);
}
