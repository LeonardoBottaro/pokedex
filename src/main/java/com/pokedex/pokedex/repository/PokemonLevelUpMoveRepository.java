package com.pokedex.pokedex.repository;

// Importa a entidade dos movimentos aprendidos por nível.
import com.pokedex.pokedex.model.PokemonLevelUpMove;

// Importa a interface de persistência do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa a lista usada para retornar vários registros.
import java.util.List;

// Define as operações de acesso aos movimentos por nível.
public interface PokemonLevelUpMoveRepository
        extends JpaRepository<PokemonLevelUpMove, Integer> {

    // Busca os movimentos de um Pokémon, ordenados pelo nível.
    List<PokemonLevelUpMove> findByPokemonNameOrderByLevelAsc(String name);

    // Busca os Pokémon que aprendem determinado movimento.
    List<PokemonLevelUpMove> findByMoveName(String name);

    // Verifica se o Pokémon já possui esse movimento nesse nível.
    boolean existsByPokemonIdAndMoveIdAndLevel(
            Integer pokemonId,
            Integer moveId,
            Integer level
    );
}
