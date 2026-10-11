// Cliente HTTP único: injeta o token JWT e padroniza os erros da API (RFC 7807).
const BASE = import.meta.env?.VITE_API_URL ?? '';
const CHAVE_SESSAO = 'logitrack.sessao';

export const sessao = {
  obter() {
    try { return JSON.parse(sessionStorage.getItem(CHAVE_SESSAO)); } catch { return null; }
  },
  salvar(dados) { sessionStorage.setItem(CHAVE_SESSAO, JSON.stringify(dados)); },
  limpar() { sessionStorage.removeItem(CHAVE_SESSAO); },
};

export class ApiError extends Error {
  constructor(status, corpo) {
    super(corpo?.detail || `Erro ${status}`);
    this.status = status;
    this.campos = corpo?.campos || {};
  }
}

async function request(metodo, caminho, corpo) {
  const token = sessao.obter()?.token;
  const resp = await fetch(`${BASE}${caminho}`, {
    method: metodo,
    headers: {
      ...(corpo ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: corpo ? JSON.stringify(corpo) : undefined,
  });

  if (resp.status === 401 && caminho !== '/api/auth/login') {
    sessao.limpar();
    window.dispatchEvent(new Event('logitrack:logout'));
  }
  if (!resp.ok) {
    const erro = await resp.json().catch(() => null);
    throw new ApiError(resp.status, erro);
  }
  return resp.status === 204 ? null : resp.json();
}

const qs = (params) => {
  const limpo = Object.entries(params).filter(([, v]) => v !== '' && v != null);
  return limpo.length ? `?${new URLSearchParams(limpo)}` : '';
};

export const api = {
  login: (email, senha) => request('POST', '/api/auth/login', { email, senha }),

  veiculos: {
    listar: () => request('GET', '/api/veiculos'),
    resumo: (filtros) => request('GET', `/api/veiculos/resumo${qs(filtros)}`),
    criar: (dados) => request('POST', '/api/veiculos', dados),
    atualizar: (id, dados) => request('PUT', `/api/veiculos/${id}`, dados),
    excluir: (id) => request('DELETE', `/api/veiculos/${id}`),
  },

  manutencoes: {
    listar: (filtros) => request('GET', `/api/manutencoes${qs(filtros)}`),
    criar: (dados) => request('POST', '/api/manutencoes', dados),
    atualizar: (id, dados) => request('PUT', `/api/manutencoes/${id}`, dados),
    alterarStatus: (id, status) => request('PATCH', `/api/manutencoes/${id}/status`, { status }),
    excluir: (id) => request('DELETE', `/api/manutencoes/${id}`),
  },

  dashboard: {
    resumo: () => request('GET', '/api/dashboard'),
    totalKm: (veiculoId) => request('GET', `/api/dashboard/km-total${qs({ veiculoId })}`),
  },
};
