
package com.pokedex.pokedex.repository;

import com.pokedex.pokedex.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoveRepository extends JpaRepository<Move, Integer> {

    // Busca pelo nome exato, mantendo compatibilidade com o importador.
    Optional<Move> findByName(String name);

    // Busca pelo nome exato, ignorando maiúsculas e minúsculas.
    Optional<Move> findByNameIgnoreCase(String name);

    // Pesquisa por parte do nome.
    List<Move> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    // Filtra pelo tipo elemental.
    List<Move> findByTypeIgnoreCaseOrderByNameAsc(String type);

    // Filtra pela categoria: PHYSICAL, SPECIAL ou STATUS.
    List<Move> findByCategoryIgnoreCaseOrderByNameAsc(String category);
}
