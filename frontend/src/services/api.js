
const API_URL = "http://localhost:8080/api";

async function requisicao(endpoint) {
  const resposta = await fetch(`${API_URL}${endpoint}`);

  if (!resposta.ok) {
    throw new Error(
      `Erro na API: ${resposta.status} ${resposta.statusText}`
    );
  }

  return resposta.json();
}

export const pokemonApi = {
  listarTodos() {
    return requisicao("/pokemon");
  },

  buscarPorNome(nome) {
    return requisicao(`/pokemon/${encodeURIComponent(nome)}`);
  },

  buscarPorParteDoNome(nome) {
    return requisicao(
      `/pokemon/buscar?nome=${encodeURIComponent(nome)}`
    );
  },
};

export const movimentosApi = {
  listarTodos() {
    return requisicao("/moves");
  },

  buscarPorNome(nome) {
    return requisicao(`/moves/${encodeURIComponent(nome)}`);
  },

  buscarPorFiltros({ nome, tipo, categoria } = {}) {
    const parametros = new URLSearchParams();

    if (nome) parametros.set("nome", nome);
    if (tipo) parametros.set("tipo", tipo);
    if (categoria) parametros.set("categoria", categoria);

    return requisicao(`/moves/buscar?${parametros.toString()}`);
  },
};

export const golpesPokemonApi = {
  porNivel(nomePokemon) {
    return requisicao(
      `/level-up-moves/${encodeURIComponent(nomePokemon)}`
    );
  },

  porTmHm(nomePokemon) {
    return requisicao(
      `/tm-moves/${encodeURIComponent(nomePokemon)}`
    );
  },
};

export const evolucoesApi = {
  listarTodas() {
    return requisicao("/evolutions");
  },

  porPokemon(nomePokemon) {
    return requisicao(
      `/evolutions/${encodeURIComponent(nomePokemon)}`
    );
  },
};
