package com.pokedex.pokedex.model;

// Importa as anotações utilizadas para mapear a entidade no banco.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Indica que esta classe representa uma entidade persistida no banco.
@Entity

// Define a tabela que armazenará os movimentos aprendidos por nível.
@Table(name = "pokemon_level_up_moves")
public class PokemonLevelUpMove {

    // Identificador único do registro no banco de dados.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Pokémon que aprende o movimento.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pokemon_id", nullable = false)
    private Pokemon pokemon;

    // Movimento aprendido pelo Pokémon.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "move_id", nullable = false)
    private Move move;

    // Nível em que o Pokémon aprende o movimento.
    @Column(nullable = false)
    private Integer level;

    // Construtor vazio exigido pelo JPA.
    public PokemonLevelUpMove() {
    }

    // Retorna o identificador do registro.
    public Integer getId() {
        return id;
    }

    // Define o identificador do registro.
    public void setId(Integer id) {
        this.id = id;
    }

    // Retorna o Pokémon associado ao registro.
    public Pokemon getPokemon() {
        return pokemon;
    }

    // Define o Pokémon associado ao registro.
    public void setPokemon(Pokemon pokemon) {
        this.pokemon = pokemon;
    }

    // Retorna o movimento associado ao registro.
    public Move getMove() {
        return move;
    }

    // Define o movimento associado ao registro.
    public void setMove(Move move) {
        this.move = move;
    }

    // Retorna o nível de aprendizado.
    public Integer getLevel() {
        return level;
    }

    // Define o nível de aprendizado.
    public void setLevel(Integer level) {
        this.level = level;
    }
}
