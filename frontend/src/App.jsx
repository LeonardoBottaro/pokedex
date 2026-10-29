import { useEffect, useMemo, useState } from "react";
import "./App.css";

const API_URL = "http://localhost:8080/api";

const typeOptions = [
  "NORMAL",
  "FIRE",
  "WATER",
  "ELECTRIC",
  "GRASS",
  "ICE",
  "FIGHTING",
  "POISON",
  "GROUND",
  "FLYING",
  "PSYCHIC",
  "BUG",
  "ROCK",
  "GHOST",
  "DRAGON",
  "DARK",
  "STEEL",
  "FAIRY",
];

const menuItems = [
  { label: "Pokédex", icon: "▣" },
  { label: "Moves", icon: "⚔" },
  { label: "Items", icon: "◉" },
  { label: "Abilities", icon: "✦" },
  { label: "Locations", icon: "⌖" },
  { label: "Evolutions", icon: "♧" },
  { label: "Egg Groups", icon: "◈" },
  { label: "Stats", icon: "▥" },
  { label: "Types", icon: "◇" },
  { label: "Trainers", icon: "♟" },
  { label: "Map", icon: "⌖" },
  { label: "Events", icon: "◷" },
];

function formatarNome(nome) {
  if (!nome) return "Desconhecido";

  return nome
    .replace(/_plain\b/gi, "") // Remove "_plain" do nome
    .replace(/_/g, " ") // Substitui os "_" restantes por espaços
    .trim()
    .toLowerCase()
    .replace(/^./, (letra) => letra.toUpperCase());
}

// Converte os dados recebidos da API para o formato usado pela interface.
function adaptarPokemon(pokemon) {
  const numero = pokemon.johtoDexNumber ?? pokemon.id;

  return {
    id: pokemon.id,
    number: numero != null ? String(numero).padStart(3, "0") : "---",
    name: formatarNome(pokemon.name),
    types: [pokemon.type1, pokemon.type2]
      .filter((type) => type && type.trim() !== "")
      .map((type) => type.toUpperCase()),
    total: pokemon.total ?? 0,
    hp: pokemon.hp ?? 0,
    attack: pokemon.attack ?? 0,
    defense: pokemon.defense ?? 0,
    specialAttack: pokemon.specialAttack ?? 0,
    specialDefense: pokemon.specialDefense ?? 0,
    speed: pokemon.speed ?? 0,
    item1: pokemon.item1,
    item2: pokemon.item2,
  };
}

function App() {
  const [activeSection, setActiveSection] = useState("Pokédex");
  const [search, setSearch] = useState("");
  const [type1, setType1] = useState("");
  const [type2, setType2] = useState("");
  const [selectedPokemon, setSelectedPokemon] = useState(null);
  const [activeTab, setActiveTab] = useState("INFO");

  const [pokemonList, setPokemonList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Busca os Pokémon na API quando a interface é carregada.
  useEffect(() => {
    const controller = new AbortController();

    async function carregarPokemon() {
      try {
        setLoading(true);
        setError("");

        const resposta = await fetch(`${API_URL}/pokemon`, {
          signal: controller.signal,
        });

        if (!resposta.ok) {
          throw new Error(`A API retornou o erro ${resposta.status}.`);
        }

        const dados = await resposta.json();

        if (!Array.isArray(dados)) {
          throw new Error("A resposta da API não contém uma lista de Pokémon.");
        }

        // Ordena os Pokémon pelo número da Pokédex, em ordem crescente.
        const dadosOrdenados = [...dados].sort((a, b) => {
          const numeroA = Number(a.johtoDexNumber ?? a.id);
          const numeroB = Number(b.johtoDexNumber ?? b.id);

          return numeroA - numeroB;
        });

        // Adapta os dados já ordenados para o formato da interface.

        const listaAdaptada = dadosOrdenados
          .filter((pokemon) => {
            const nome = (pokemon.name ?? "").toLowerCase();

            // Exclui as formas regionais e variantes que não queremos exibir.
            const formasExcluidas =
              /_(hisuian|galarian|alolan|paldean|paldean_water|paldean_fire|bloodmoon)$/i;

            return !formasExcluidas.test(nome);
          })
          .map(adaptarPokemon);

        setPokemonList(listaAdaptada);

        // Atualiza os detalhes se o Pokémon selecionado estiver na lista.
        setSelectedPokemon((atual) => {
          if (!atual) return null;

          return (
            listaAdaptada.find((pokemon) => pokemon.id === atual.id) ?? null
          );
        });
      } catch (erro) {
        if (erro.name === "AbortError") return;

        console.error("Erro ao carregar Pokémon:", erro);

        setError(
          "Não foi possível carregar os Pokémon. Verifique se o Spring Boot está rodando e se a API está acessível.",
        );
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    carregarPokemon();

    return () => controller.abort();
  }, []);

  const filteredPokemon = useMemo(() => {
    const normalizedSearch = search.trim().toLocaleLowerCase();

    return pokemonList.filter((pokemon) => {
      const matchesName =
        !normalizedSearch ||
        pokemon.name.toLocaleLowerCase().includes(normalizedSearch) ||
        pokemon.number.includes(normalizedSearch.replace("#", ""));

      const matchesType1 = !type1 || pokemon.types[0] === type1;

      const matchesType2 =
        !type2 ||
        (type2 === "NONE"
          ? pokemon.types.length === 1
          : pokemon.types[1] === type2);

      return matchesName && matchesType1 && matchesType2;
    });
  }, [pokemonList, search, type1, type2]);

  function clearFilters() {
    setSearch("");
    setType1("");
    setType2("");
  }

  function openPokemon(pokemon) {
    setSelectedPokemon(pokemon);
    setActiveTab("INFO");
  }

  function formatItem(item) {
    return item && item.trim() !== "" ? item : "NENHUM";
  }

  function renderTypes(pokemon) {
    if (!pokemon.types.length) {
      return <span>SEM TIPO CADASTRADO</span>;
    }

    return pokemon.types.map((type) => (
      <span className={`type-tag type-${type.toLowerCase()}`} key={type}>
        {type}
      </span>
    ));
  }

  return (
    <main className="app-layout">
      <section className="main-panel">
        {activeSection === "Pokédex" ? (
          selectedPokemon ? (
            <section className="pokedex-screen detail-screen">
              <button
                className="back-button"
                onClick={() => setSelectedPokemon(null)}
              >
                ← VOLTAR À LISTA
              </button>

              <header className="pokedex-header">
                <div className="header-title">
                  <span className="header-mark">◆</span>
                  <h1>POKéDEX</h1>
                </div>

                <div className="pokemon-number">
                  <span>NO.</span>
                  <strong>{selectedPokemon.number}</strong>
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
                  <div className="pokemon-art">
                    <span className="art-number">
                      #{selectedPokemon.number}
                    </span>

                    <div
                      className="sprite-placeholder"
                      role="img"
                      aria-label={`Imagem de ${selectedPokemon.name} ainda não disponível`}
                    >
                      <span className="placeholder-symbol">?</span>
                    </div>

                    <span className="art-caption">IMAGEM EM BREVE</span>
                  </div>

                  <div className="pokemon-details">
                    <p className="detail-label">POKÉMON</p>
                    <h2 className="pokemon-name">{selectedPokemon.name}</h2>

                    <p className="detail-label">TYPE</p>

                    <div className="type-list">
                      {renderTypes(selectedPokemon)}
                    </div>

                    <div className="category">
                      <span className="detail-label">TOTAL DOS ATRIBUTOS</span>
                      <strong>{selectedPokemon.total}</strong>
                    </div>

                    <div className="category">
                      <span className="detail-label">ITEM 1</span>
                      <strong>{formatItem(selectedPokemon.item1)}</strong>
                    </div>

                    <div className="category">
                      <span className="detail-label">ITEM 2</span>
                      <strong>{formatItem(selectedPokemon.item2)}</strong>
                    </div>
                  </div>
                </section>
              ) : (
                <section className="data-content">
                  <div className="data-heading">
                    <span className="data-pokeball">◉</span>
                    <div>
                      <p className="detail-label">POKÉMON DATA</p>
                      <h2>{selectedPokemon.name}</h2>
                    </div>
                  </div>

                  <div className="data-entry">
                    <span>POKÉDEX NUMBER</span>
                    <strong>#{selectedPokemon.number}</strong>
                  </div>

                  <div className="data-entry">
                    <span>ID NO BANCO</span>
                    <strong>{selectedPokemon.id}</strong>
                  </div>

                  <div className="data-entry">
                    <span>TYPE</span>
                    <strong>
                      {selectedPokemon.types.join(" / ") || "NÃO CADASTRADO"}
                    </strong>
                  </div>

                  <div className="data-entry">
                    <span>HP</span>
                    <strong>{selectedPokemon.hp}</strong>
                  </div>

                  <div className="data-entry">
                    <span>ATTACK</span>
                    <strong>{selectedPokemon.attack}</strong>
                  </div>

                  <div className="data-entry">
                    <span>DEFENSE</span>
                    <strong>{selectedPokemon.defense}</strong>
                  </div>

                  <div className="data-entry">
                    <span>SPECIAL ATTACK</span>
                    <strong>{selectedPokemon.specialAttack}</strong>
                  </div>

                  <div className="data-entry">
                    <span>SPECIAL DEFENSE</span>
                    <strong>{selectedPokemon.specialDefense}</strong>
                  </div>

                  <div className="data-entry">
                    <span>SPEED</span>
                    <strong>{selectedPokemon.speed}</strong>
                  </div>

                  <div className="data-entry">
                    <span>TOTAL</span>
                    <strong>{selectedPokemon.total}</strong>
                  </div>

                  <div className="description-box">
                    <span className="detail-label">HELD ITEMS</span>
                    <p>Item 1: {formatItem(selectedPokemon.item1)}</p>
                    <p>Item 2: {formatItem(selectedPokemon.item2)}</p>
                  </div>
                </section>
              )}
            </section>
          ) : (
            <section className="pokedex-screen list-screen">
              <header className="pokedex-header">
                <div className="header-title">
                  <span className="header-mark">◆</span>
                  <h1>POKéDEX</h1>
                </div>

                <div className="pokemon-number">
                  <span>RESULTADOS</span>
                  <strong>{loading ? "..." : filteredPokemon.length}</strong>
                </div>
              </header>

              <div className="screen-divider" />

              <section
                className="search-panel"
                aria-label="Buscar e filtrar Pokémon"
              >
                <label className="search-field">
                  <span className="visually-hidden">
                    Buscar Pokémon por nome ou número
                  </span>

                  <span className="search-icon">⌕</span>

                  <input
                    value={search}
                    onChange={(event) => setSearch(event.target.value)}
                    placeholder="Buscar Pokémon por nome ou número..."
                    disabled={loading || Boolean(error)}
                  />
                </label>

                <div className="filter-row">
                  <label className="filter-field">
                    <span>TIPO 1</span>

                    <select
                      value={type1}
                      onChange={(event) => setType1(event.target.value)}
                      disabled={loading || Boolean(error)}
                    >
                      <option value="">Todos os tipos</option>

                      {typeOptions.map((type) => (
                        <option key={type} value={type}>
                          {type}
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="filter-field">
                    <span>TIPO 2</span>

                    <select
                      value={type2}
                      onChange={(event) => setType2(event.target.value)}
                      disabled={loading || Boolean(error)}
                    >
                      <option value="">Todos os tipos</option>
                      <option value="NONE">Sem segundo tipo</option>

                      {typeOptions.map((type) => (
                        <option key={type} value={type}>
                          {type}
                        </option>
                      ))}
                    </select>
                  </label>

                  <button className="clear-filters" onClick={clearFilters}>
                    LIMPAR
                  </button>
                </div>
              </section>

              <div className="list-summary">
                <span>
                  {loading
                    ? "CARREGANDO POKÉMON..."
                    : "SELECIONE UM POKÉMON PARA VER OS DETALHES"}
                </span>
                <span>
                  {loading ? "..." : `${filteredPokemon.length} ENCONTRADO(S)`}
                </span>
              </div>

              <section className="pokemon-list" aria-label="Lista de Pokémon">
                {loading ? (
                  <div className="empty-state">
                    <strong>CARREGANDO POKÉDEX...</strong>
                    <p>Buscando os dados no servidor.</p>
                  </div>
                ) : error ? (
                  <div className="empty-state">
                    <span className="empty-icon">!</span>
                    <strong>ERRO AO CARREGAR A POKÉDEX</strong>
                    <p>{error}</p>

                    <button
                      className="clear-filters"
                      onClick={() => window.location.reload()}
                    >
                      TENTAR NOVAMENTE
                    </button>
                  </div>
                ) : filteredPokemon.length ? (
                  filteredPokemon.map((pokemon) => (
                    <button
                      className="pokemon-row"
                      key={pokemon.id}
                      onClick={() => openPokemon(pokemon)}
                    >
                      <span className="list-number">{pokemon.number}</span>

                      <span className="list-sprite" aria-hidden="true">
                        ?
                      </span>

                      <span className="list-info">
                        <strong>{pokemon.name}</strong>

                        <span className="list-types">
                          {renderTypes(pokemon)}
                        </span>
                      </span>

                      <span className="row-chevron">›</span>
                    </button>
                  ))
                ) : (
                  <div className="empty-state">
                    <span className="empty-icon">⌕</span>
                    <strong>NENHUM POKÉMON ENCONTRADO</strong>
                    <p>Tente outro nome ou altere os filtros de tipo.</p>

                    <button className="clear-filters" onClick={clearFilters}>
                      LIMPAR BUSCA E FILTROS
                    </button>
                  </div>
                )}
              </section>
            </section>
          )
        ) : (
          <section className="pokedex-screen section-placeholder">
            <header className="pokedex-header">
              <div className="header-title">
                <span className="header-mark">◆</span>
                <h1>{activeSection.toUpperCase()}</h1>
              </div>
            </header>

            <div className="screen-divider" />

            <div className="coming-soon">
              <span>◈</span>
              <h2>{activeSection.toUpperCase()}</h2>
              <p>Esta seção será implementada em uma próxima etapa.</p>

              <button
                className="back-button"
                onClick={() => setActiveSection("Pokédex")}
              >
                VOLTAR À POKÉDEX
              </button>
            </div>
          </section>
        )}
      </section>

      <aside className="sidebar" aria-label="Menu principal">
        <div className="sidebar-heading">
          <span className="sidebar-logo">◆</span>
          <span>POKéDEX</span>
        </div>

        <nav className="sidebar-menu">
          {menuItems.map((item) => (
            <button
              key={item.label}
              className={`menu-button ${
                activeSection === item.label ? "selected" : ""
              }`}
              onClick={() => {
                setActiveSection(item.label);
                setSelectedPokemon(null);
              }}
            >
              <span className="menu-icon" aria-hidden="true">
                {item.icon}
              </span>
              <span>{item.label}</span>
            </button>
          ))}
        </nav>

        <div className="sidebar-footer">
          POLISHED CRYSTAL
          <br />
          <span>POKÉDEX DATABASE</span>
        </div>
      </aside>
    </main>
  );
}

export default App;
