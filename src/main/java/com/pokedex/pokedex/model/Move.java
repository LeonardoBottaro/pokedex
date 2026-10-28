
package com.pokedex.pokedex.model;

// Importa as anotações utilizadas para mapear a entidade no banco.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Indica que esta classe representa uma entidade persistida no banco.
@Entity

// Define que a entidade será associada à tabela "moves".
@Table(name = "moves")
public class Move {

    // Identificador único do movimento no banco de dados.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nome interno do movimento, como FIRE_PUNCH.
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    // Identificador do efeito definido no arquivo ASM.
    @Column(nullable = false, length = 100)
    private String effect;

    // Poder base do movimento.
    @Column(nullable = false)
    private Integer power;

    // Tipo elemental do movimento, como FIRE ou WATER.
    @Column(nullable = false, length = 20)
    private String type;

    // Precisão do movimento; -1 representa uma exceção à verificação.
    @Column(nullable = false)
    private Integer accuracy;

    // Pontos de poder disponíveis para usar o movimento.
    @Column(nullable = false)
    private Integer pp;

    // Chance percentual de ativar o efeito adicional.
    @Column(name = "effect_chance", nullable = false)
    private Integer effectChance;

    // Categoria do movimento: PHYSICAL, SPECIAL ou STATUS.
    @Column(nullable = false, length = 20)
    private String category;

    // Construtor vazio exigido pelo JPA para criar instâncias da entidade.
    public Move() {
    }

    // Retorna o identificador do movimento.
    public Integer getId() {
        return id;
    }

    // Define o identificador do movimento.
    public void setId(Integer id) {
        this.id = id;
    }

    // Retorna o nome interno do movimento.
    public String getName() {
        return name;
    }

    // Define o nome interno do movimento.
    public void setName(String name) {
        this.name = name;
    }

    // Retorna o efeito do movimento.
    public String getEffect() {
        return effect;
    }

    // Define o efeito do movimento.
    public void setEffect(String effect) {
        this.effect = effect;
    }

    // Retorna o poder base do movimento.
    public Integer getPower() {
        return power;
    }

    // Define o poder base do movimento.
    public void setPower(Integer power) {
        this.power = power;
    }

    // Retorna o tipo elemental do movimento.
    public String getType() {
        return type;
    }

    // Define o tipo elemental do movimento.
    public void setType(String type) {
        this.type = type;
    }

    // Retorna a precisão do movimento.
    public Integer getAccuracy() {
        return accuracy;
    }

    // Define a precisão do movimento.
    public void setAccuracy(Integer accuracy) {
        this.accuracy = accuracy;
    }

    // Retorna os pontos de poder do movimento.
    public Integer getPp() {
        return pp;
    }

    // Define os pontos de poder do movimento.
    public void setPp(Integer pp) {
        this.pp = pp;
    }

    // Retorna a chance de ativar o efeito adicional.
    public Integer getEffectChance() {
        return effectChance;
    }

    // Define a chance de ativar o efeito adicional.
    public void setEffectChance(Integer effectChance) {
        this.effectChance = effectChance;
    }

    // Retorna a categoria do movimento.
    public String getCategory() {
        return category;
    }

    // Define a categoria do movimento.
    public void setCategory(String category) {
        this.category = category;
    }
}
