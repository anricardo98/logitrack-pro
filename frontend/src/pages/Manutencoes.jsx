import { useCallback, useEffect, useState } from 'react';
import { api } from '../api.js';
import { Icone } from '../components/Icone.jsx';
import { Carregando, ErroCarga, Modal, StatusBadge, mensagemDeCarga } from '../components/Ui.jsx';
import { STATUS, formatarData, formatarMoeda } from '../format.js';
import ManutencaoForm from './ManutencaoForm.jsx';

export default function Manutencoes() {
  const [veiculos, setVeiculos] = useState([]);
  const [filtros, setFiltros] = useState({ status: '', veiculoId: '', page: 0 });
  const [pagina, setPagina] = useState(null);
  const [editando, setEditando] = useState(null);   // null = fechado | {} = nova | objeto = edição
  const [excluindo, setExcluindo] = useState(null);
  const [aviso, setAviso] = useState('');
  
  const [erroCarga, setErroCarga] = useState('');

  const manutencoesPendentes = pagina?.content.filter((m) => m.status === 'PENDENTE').length || 0;

  const carregar = useCallback(() => {
    setErroCarga('');
    api.manutencoes.listar({ ...filtros, size: 8, sort: 'dataInicio,desc' })
      .then(setPagina)
      .catch((e) => setErroCarga(mensagemDeCarga(e)));
  }, [filtros]);

  const carregarVeiculos = useCallback(() => { api.veiculos.listar().then(setVeiculos); }, []);
  useEffect(() => { carregarVeiculos(); }, [carregarVeiculos]);
  useEffect(() => { carregar(); }, [carregar]);

  const notificar = (msg) => { setAviso(msg); setTimeout(() => setAviso(''), 3000); };
  const filtrar = (campo, valor) => setFiltros((f) => ({ ...f, [campo]: valor, page: 0 }));

  function salvo(msg, { veiculoNovo } = {}) {
    setEditando(null);
    notificar(veiculoNovo ? `${msg} Veículo novo cadastrado.` : msg);
    carregar();
    if (veiculoNovo) carregarVeiculos();   // o novo veículo passa a aparecer no filtro
  }

  async function trocarStatus(m, status) {
    try {
      await api.manutencoes.alterarStatus(m.id, status);
      notificar('Status atualizado.');
      carregar();
    } catch (e) { notificar(e.message); }
  }

  async function confirmarExclusao() {
    await api.manutencoes.excluir(excluindo.id);
    setExcluindo(null);
    notificar('Manutenção excluída.');
    carregar();
  }

  return (
    <div className="card">
      <div className="card card-cabecalho">
        <span>Número de manuntenções pendentes:</span>
        <span style={{ color: manutencoesPendentes > 0 ? 'var(--vermelho)' : 'var(--verde)' }}> {manutencoesPendentes} </span>
      </div>
      <div className="barra-ferramentas">
        <div className="filtros">
          <Icone nome="filtro" />
          <select value={filtros.veiculoId} onChange={(e) => filtrar('veiculoId', e.target.value)}>
            <option value="">Todos os veículos</option>
            {veiculos.map((v) => <option key={v.id} value={v.id}>{v.placa} · {v.modelo}</option>)}
          </select>
          <div className="segmentado">
            {[['', 'Todos'], ...Object.entries(STATUS).map(([k, v]) => [k, v.rotulo])].map(([k, r]) => (
              <button key={k} className={filtros.status === k ? 'ativo' : ''} onClick={() => filtrar('status', k)}>{r}</button>
            ))}
          </div>
        </div>
        <button className="btn primario" onClick={() => setEditando({})}>
          <Icone nome="mais" /> Agendar manutenção
        </button>
      </div>

      {erroCarga ? <ErroCarga mensagem={erroCarga} onTentar={carregar} /> : !pagina ? <Carregando /> : pagina.content.length === 0 ? (
        <div className="vazio">Nenhuma manutenção encontrada para os filtros selecionados.</div>
      ) : (
        <div className="tabela-scroll">
          <table className="tabela">
            <thead>
              <tr>
                <th>Veículo</th><th>Serviço</th><th>Início</th><th>Finalização prevista</th>
                <th className="dir">Custo estimado</th><th>Status</th><th />
              </tr>
            </thead>
            <tbody>
              {pagina.content.map((m) => (
                <tr key={m.id}>
                  <td>
                    <div className="celula-veiculo">
                      <span className={`tag-tipo ${m.veiculo.tipo === 'PESADO' ? 'pesado' : 'leve'}`}>{m.veiculo.tipo === 'PESADO' ? 'P' : 'L'}</span>
                      <div><strong>{m.veiculo.placa}</strong><small>{m.veiculo.modelo}</small></div>
                    </div>
                  </td>
                  <td>{m.tipoServico}</td>
                  <td>{formatarData(m.dataInicio)}</td>
                  <td>{formatarData(m.dataFinalizacao)}</td>
                  <td className="dir num">{formatarMoeda(m.custoEstimado)}</td>
                  <td>
                    <select className={`status-select ${STATUS[m.status].classe}`} value={m.status}
                      onChange={(e) => trocarStatus(m, e.target.value)}>
                      {Object.entries(STATUS).map(([k, v]) => <option key={k} value={k}>{v.rotulo}</option>)}
                    </select>
                  </td>
                  <td className="acoes">
                    <button className="btn-icone" title="Editar" onClick={() => setEditando(m)}><Icone nome="editar" /></button>
                    <button className="btn-icone perigo" title="Excluir" onClick={() => setExcluindo(m)}><Icone nome="lixeira" /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {pagina && !erroCarga && pagina.totalPages > 1 && (
        <div className="paginacao">
          <span>{pagina.totalElements} registros</span>
          <div>
            <button className="btn" disabled={pagina.first} onClick={() => setFiltros((f) => ({ ...f, page: f.page - 1 }))}>Anterior</button>
            <span>Página {pagina.number + 1} de {pagina.totalPages}</span>
            <button className="btn" disabled={pagina.last} onClick={() => setFiltros((f) => ({ ...f, page: f.page + 1 }))}>Próxima</button>
          </div>
        </div>
      )}

      {editando && (
        <Modal titulo={editando.id ? 'Editar manutenção' : 'Agendar manutenção'} onFechar={() => setEditando(null)}>
          <ManutencaoForm inicial={editando} veiculos={veiculos} onCancelar={() => setEditando(null)}
            onSalvo={(info) => salvo(editando.id ? 'Manutenção atualizada.' : 'Manutenção agendada.', info)} />
        </Modal>
      )}

      {excluindo && (
        <Modal titulo="Excluir manutenção" largura={420} onFechar={() => setExcluindo(null)}>
          <p className="modal-texto">
            Excluir <strong>{excluindo.tipoServico}</strong> do veículo <strong>{excluindo.veiculo.placa}</strong> em {formatarData(excluindo.dataInicio)}? Essa ação não pode ser desfeita.
          </p>
          <div className="modal-acoes">
            <button className="btn" onClick={() => setExcluindo(null)}>Cancelar</button>
            <button className="btn perigo" onClick={confirmarExclusao}>Excluir</button>
          </div>
        </Modal>
      )}

      {aviso && <div className="toast">{aviso}</div>}
    </div>
  );
}
