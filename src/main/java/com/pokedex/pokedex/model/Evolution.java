
package com.pokedex.pokedex.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "evolution",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_evolution",
        columnNames = {
            "from_pokemon_id",
            "to_pokemon_id",
            "method",
            "condition_value"
        }
    )
)
public class Evolution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_pokemon_id", nullable = false)
    private Pokemon fromPokemon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_pokemon_id", nullable = false)
    private Pokemon toPokemon;

    @Column(nullable = false, length = 50)
    private String method;

    @Column(name = "condition_value", length = 100)
    private String conditionValue;

    public Evolution() {
    }

    public Integer getId() {
        return id;
    }

    public Pokemon getFromPokemon() {
        return fromPokemon;
    }

    public void setFromPokemon(Pokemon fromPokemon) {
        this.fromPokemon = fromPokemon;
    }

    public Pokemon getToPokemon() {
        return toPokemon;
    }

    public void setToPokemon(Pokemon toPokemon) {
        this.toPokemon = toPokemon;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }
}
