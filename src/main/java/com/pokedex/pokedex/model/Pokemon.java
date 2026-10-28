
package com.pokedex.pokedex.model;

// Importa as anotações utilizadas para mapear a classe para o banco.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Indica que esta classe representa uma entidade persistida no banco.
@Entity

// Define o nome da tabela associada a esta entidade.
@Table(name = "pokemon")
public class Pokemon {

    // Identificador único gerado automaticamente pelo banco de dados.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nome do Pokémon, que deve ser único na tabela.
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    // Soma dos seis atributos-base do Pokémon.
    @Column(name = "total", nullable = false)
    private Integer total;

    // Pontos de vida.
    @Column(name = "hp", nullable = false)
    private Integer hp;

    // Ataque físico.
    @Column(name = "attack", nullable = false)
    private Integer attack;

    // Defesa física.
    @Column(name = "defense", nullable = false)
    private Integer defense;

    // Velocidade.
    @Column(name = "speed", nullable = false)
    private Integer speed;

    // Ataque especial.
    @Column(name = "special_attack", nullable = false)
    private Integer specialAttack;

    // Defesa especial.
    @Column(name = "special_defense", nullable = false)
    private Integer specialDefense;

    // Primeiro tipo do Pokémon.
    @Column(name = "type1", nullable = false, length = 20)
    private String type1;

    // Segundo tipo; pode ficar vazio para Pokémon de tipo único.
    @Column(name = "type2", length = 20)
    private String type2;

    // Primeiro item que o Pokémon pode carregar.
    @Column(name = "item1", length = 50)
    private String item1;

    // Segundo item que o Pokémon pode carregar.
    @Column(name = "item2", length = 50)
    private String item2;

    // Número na Pokédex de Johto; pode ficar vazio enquanto validamos os dados.
    @Column(name = "johto_dex_number")
    private Integer johtoDexNumber;

    // Construtor vazio exigido pelo JPA para criar instâncias da entidade.
    public Pokemon() {
    }

    // Retorna o identificador do Pokémon.
    public Integer getId() {
        return id;
    }

    // Define o identificador do Pokémon.
    public void setId(Integer id) {
        this.id = id;
    }

    // Retorna o nome do Pokémon.
    public String getName() {
        return name;
    }

    // Define o nome do Pokémon.
    public void setName(String name) {
        this.name = name;
    }

    // Retorna a soma dos atributos-base.
    public Integer getTotal() {
        return total;
    }

    // Define a soma dos atributos-base.
    public void setTotal(Integer total) {
        this.total = total;
    }

    // Retorna os pontos de vida.
    public Integer getHp() {
        return hp;
    }

    // Define os pontos de vida.
    public void setHp(Integer hp) {
        this.hp = hp;
    }

    // Retorna o ataque físico.
    public Integer getAttack() {
        return attack;
    }

    // Define o ataque físico.
    public void setAttack(Integer attack) {
        this.attack = attack;
    }

    // Retorna a defesa física.
    public Integer getDefense() {
        return defense;
    }

    // Define a defesa física.
    public void setDefense(Integer defense) {
        this.defense = defense;
    }

    // Retorna a velocidade.
    public Integer getSpeed() {
        return speed;
    }

    // Define a velocidade.
    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    // Retorna o ataque especial.
    public Integer getSpecialAttack() {
        return specialAttack;
    }

    // Define o ataque especial.
    public void setSpecialAttack(Integer specialAttack) {
        this.specialAttack = specialAttack;
    }

    // Retorna a defesa especial.
    public Integer getSpecialDefense() {
        return specialDefense;
    }

    // Define a defesa especial.
    public void setSpecialDefense(Integer specialDefense) {
        this.specialDefense = specialDefense;
    }

    // Retorna o primeiro tipo.
    public String getType1() {
        return type1;
    }

    // Define o primeiro tipo.
    public void setType1(String type1) {
        this.type1 = type1;
    }

    // Retorna o segundo tipo.
    public String getType2() {
        return type2;
    }

    // Define o segundo tipo.
    public void setType2(String type2) {
        this.type2 = type2;
    }

    // Retorna o primeiro item carregado.
    public String getItem1() {
        return item1;
    }

    // Define o primeiro item carregado.
    public void setItem1(String item1) {
        this.item1 = item1;
    }

    // Retorna o segundo item carregado.
    public String getItem2() {
        return item2;
    }

    // Define o segundo item carregado.
    public void setItem2(String item2) {
        this.item2 = item2;
    }

    // Retorna o número na Pokédex de Johto.
    public Integer getJohtoDexNumber() {
        return johtoDexNumber;
    }

    // Define o número na Pokédex de Johto.
    public void setJohtoDexNumber(Integer johtoDexNumber) {
        this.johtoDexNumber = johtoDexNumber;
    }
}
