package com.pokedex.pokedex.repository;

// Importa a entidade que representa os movimentos.
import com.pokedex.pokedex.model.Move;

// Importa a interface que fornece operações de persistência do Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

// Importa a classe utilizada para representar um resultado que pode não existir.
import java.util.Optional;

// Define o repositório responsável por acessar a tabela de movimentos.
public interface MoveRepository extends JpaRepository<Move, Integer> {

    // Procura um movimento pelo nome interno, como FIRE_PUNCH.
    // O Optional indica que o movimento pode não existir no banco.
    Optional<Move> findByName(String name);
}
