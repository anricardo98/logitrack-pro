import { useEffect } from 'react';
import { STATUS } from '../format.js';
import { Icone } from './Icone.jsx';

export function StatusBadge({ status }) {
  const s = STATUS[status] ?? { rotulo: status, classe: '' };
  return <span className={`badge ${s.classe}`}>{s.rotulo}</span>;
}

export function Modal({ titulo, onFechar, children, largura = 560 }) {
  useEffect(() => {
    const esc = (e) => e.key === 'Escape' && onFechar();
    window.addEventListener('keydown', esc);
    return () => window.removeEventListener('keydown', esc);
  }, [onFechar]);

  return (
    <div className="modal-fundo" onMouseDown={(e) => e.target === e.currentTarget && onFechar()}>
      <div className="modal" style={{ maxWidth: largura }} role="dialog" aria-modal="true">
        <header>
          <h2>{titulo}</h2>
          <button className="btn-icone" onClick={onFechar} title="Fechar"><Icone nome="fechar" /></button>
        </header>
        {children}
      </div>
    </div>
  );
}

export function Kpi({ icone, rotulo, valor, detalhe, cor = 'azul', children }) {
  return (
    <div className="card kpi">
      <div className="kpi-topo">
        <span className={`kpi-icone ${cor}`}><Icone nome={icone} /></span>
        <span className="kpi-rotulo">{rotulo}</span>
      </div>
      <div className="kpi-valor">{valor}</div>
      {detalhe && <div className="kpi-detalhe">{detalhe}</div>}
      {children}
    </div>
  );
}

export const Carregando = () => <div className="vazio"><span className="spinner" /> Carregando…</div>;

export function mensagemDeCarga(erro) {
  if (erro?.status === 404 || erro?.status === 405)
    return 'A API não reconheceu esta rota. O backend em execução pode estar desatualizado: pare e rode "mvn spring-boot:run" de novo.';
  if (erro?.status >= 500) return 'Erro no servidor ao carregar os dados. Veja o log do backend.';
  if (erro instanceof TypeError) return 'Não foi possível conectar à API. Verifique se o backend está rodando na porta 8080.';
  return erro?.message || 'Não foi possível carregar os dados.';
}

export function ErroCarga({ mensagem, onTentar }) {
  return (
    <div className="vazio erro-carga">
      <div className="alerta erro">{mensagem}</div>
      <button className="btn" onClick={onTentar}>Tentar de novo</button>
    </div>
  );
}
