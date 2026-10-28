
import { useState } from "react";
import "./App.css";

const pokemonList = [
  {
    number: "001",
    name: "BULBASAUR",
    types: ["GRASS", "POISON"],
    sprite: "🌱",
    height: "0,7 m",
    weight: "6,9 kg",
    category: "SEED POKÉMON",
    description:
      "A small Pokémon that carries a seed on its back. The seed grows as it absorbs sunlight.",
    color: "grass",
  },
  {
    number: "004",
    name: "CHARMANDER",
    types: ["FIRE"],
    sprite: "🔥",
    height: "0,6 m",
    weight: "8,5 kg",
    category: "LIZARD POKÉMON",
    description:
      "The flame on its tail shows the strength of its life force. It burns brightly when healthy.",
    color: "fire",
  },
  {
    number: "007",
    name: "SQUIRTLE",
    types: ["WATER"],
    sprite: "💧",
    height: "0,5 m",
    weight: "9,0 kg",
    category: "TINY TURTLE POKÉMON",
    description:
      "It shelters inside its shell, then strikes back with powerful water attacks.",
    color: "water",
  },
  {
    number: "025",
    name: "PIKACHU",
    types: ["ELECTRIC"],
    sprite: "⚡",
    height: "0,4 m",
    weight: "6,0 kg",
    category: "MOUSE POKÉMON",
    description:
      "It stores electricity in its cheeks. When threatened, it releases electric charges.",
    color: "electric",
  },
  {
    number: "152",
    name: "CHIKORITA",
    types: ["GRASS"],
    sprite: "🍃",
    height: "0,9 m",
    weight: "6,4 kg",
    category: "LEAF POKÉMON",
    description:
      "It waves the leaf on its head to check the temperature and humidity around it.",
    color: "grass",
  },
];

function App() {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [activeTab, setActiveTab] = useState("INFO");

  const pokemon = pokemonList[currentIndex];

  function changePokemon(direction) {
    setCurrentIndex((current) => {
      return (current + direction + pokemonList.length) % pokemonList.length;
    });
  }

  return (
    <main className="pokedex-page">
      <section className="pokedex-screen" aria-label="Pokédex">
        <header className="pokedex-header">
          <div className="header-title">
            <span className="header-mark">◆</span>
            <h1>POKéDEX</h1>
          </div>

          <div className="pokemon-number">
            <span>NO.</span>
            <strong>{pokemon.number}</strong>
          </div>
        </header>

        <div className="screen-divider" />

        <nav className="pokedex-tabs" aria-label="Informações do Pokémon">
          <button
            className={activeTab === "INFO" ? "tab active" : "tab"}
            onClick={() => setActiveTab("INFO")}
          >
            INFO
          </button>

          <button
            className={activeTab === "DATA" ? "tab active" : "tab"}
            onClick={() => setActiveTab("DATA")}
          >
            DATA
          </button>
        </nav>

        {activeTab === "INFO" ? (
          <section className="pokemon-content">
            <div className={`pokemon-art ${pokemon.color}`}>
              <span className="art-number">#{pokemon.number}</span>
              <div className="sprite-placeholder" role="img" aria-label={pokemon.name}>
                {pokemon.sprite}
              </div>
              <span className="art-caption">POKéMON IMAGE</span>
            </div>

            <div className="pokemon-details">
              <p className="detail-label">POKéMON</p>
              <h2 className="pokemon-name">{pokemon.name}</h2>

              <p className="detail-label">TYPE</p>
              <div className="type-list">
                {pokemon.types.map((type) => (
                  <span className={`type-tag type-${type.toLowerCase()}`} key={type}>
                    {type}
                  </span>
                ))}
              </div>

              <div className="measurements">
                <div className="measurement">
                  <span className="detail-label">HEIGHT</span>
                  <strong>{pokemon.height}</strong>
                </div>

                <div className="measurement">
                  <span className="detail-label">WEIGHT</span>
                  <strong>{pokemon.weight}</strong>
                </div>
              </div>

              <div className="category">
                <span className="detail-label">CATEGORY</span>
                <strong>{pokemon.category}</strong>
              </div>
            </div>
          </section>
        ) : (
          <section className="data-content">
            <div className="data-heading">
              <span className="data-pokeball">◉</span>
              <div>
                <p className="detail-label">POKéMON DATA</p>
                <h2>{pokemon.name}</h2>
              </div>
            </div>

            <div className="data-entry">
              <span>POKéDEX NUMBER</span>
              <strong>#{pokemon.number}</strong>
            </div>

            <div className="data-entry">
              <span>SPECIES</span>
              <strong>{pokemon.category}</strong>
            </div>

            <div className="data-entry">
              <span>TYPE</span>
              <strong>{pokemon.types.join(" / ")}</strong>
            </div>

            <div className="data-entry">
              <span>HEIGHT</span>
              <strong>{pokemon.height}</strong>
            </div>

            <div className="data-entry">
              <span>WEIGHT</span>
              <strong>{pokemon.weight}</strong>
            </div>

            <div className="description-box">
              <span className="detail-label">DESCRIPTION</span>
              <p>{pokemon.description}</p>
            </div>
          </section>
        )}

        <footer className="pokedex-footer">
          <div className="page-indicator">
            {String(currentIndex + 1).padStart(2, "0")}
            <span>/</span>
            {String(pokemonList.length).padStart(2, "0")}
          </div>

          <div className="navigation">
            <button
              className="nav-button"
              onClick={() => changePokemon(-1)}
              aria-label="Pokémon anterior"
            >
              ◀
            </button>

            <span className="navigation-label">SELECT POKéMON</span>

            <button
              className="nav-button"
              onClick={() => changePokemon(1)}
              aria-label="Próximo Pokémon"
            >
              ▶
            </button>
          </div>
        </footer>
      </section>
    </main>
  );
}

export default App;
