const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const numero = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 });
const MESES = ['janeiro', 'fevereiro', 'março', 'abril', 'maio', 'junho', 'julho',
  'agosto', 'setembro', 'outubro', 'novembro', 'dezembro'];

export const formatarMoeda = (v) => moeda.format(Number(v ?? 0));
export const formatarKm = (v) => `${numero.format(Number(v ?? 0))} km`;
export const formatarNumero = (v) => numero.format(Number(v ?? 0));
export const nomeMes = (m) => MESES[m - 1];

/** "2026-10-07" -> "07/10/2026" sem passar por Date (evita erro de fuso). */
export const formatarData = (iso) => (iso ? iso.split('-').reverse().join('/') : '—');

/** Dias entre hoje e a data ISO (positivo = futuro). */
export function diasAte(iso) {
  const [a, m, d] = iso.split('-').map(Number);
  const alvo = Date.UTC(a, m - 1, d);
  const h = new Date();
  const hoje = Date.UTC(h.getFullYear(), h.getMonth(), h.getDate());
  return Math.round((alvo - hoje) / 86400000);
}

export const STATUS = {
  PENDENTE: { rotulo: 'Pendente', classe: 'pendente' },
  EM_REALIZACAO: { rotulo: 'Em realização', classe: 'andamento' },
  CONCLUIDA: { rotulo: 'Concluída', classe: 'concluida' },
};

export const TIPOS_SERVICO = ['Troca de Óleo', 'Troca de Pneus', 'Revisão de Motor', 'Revisão de Freios',
  'Alinhamento e Balanceamento', 'Troca de Filtros', 'Sistema Elétrico', 'Suspensão'];

export const TIPOS_VEICULO = { LEVE: 'Leve', PESADO: 'Pesado' };
