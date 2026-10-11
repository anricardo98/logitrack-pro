import { useCallback, useEffect, useState } from 'react';
import { api } from '../api.js';
import { Icone } from '../components/Icone.jsx';
import { Carregando, Modal } from '../components/Ui.jsx';
import { TIPOS_VEICULO } from '../format.js';
import VeiculoForm from './VeiculoForm.jsx';

export default function Veiculos() {
  const [filtros, setFiltros] = useState({ tipo: '', busca: '' });
  const [busca, setBusca] = useState('');            // texto digitado (aplicado com atraso)
  const [lista, setLista] = useState(null);
  const [editando, setEditando] = useState(null);    // null = fechado | {} = novo | objeto = edição
  const [excluindo, setExcluindo] = useState(null);
  const [erroExclusao, setErroExclusao] = useState('');
  const [aviso, setAviso] = useState('');

  const carregar = useCallback(() => { api.veiculos.resumo(filtros).then(setLista); }, [filtros]);
  useEffect(() => { carregar(); }, [carregar]);

  // "debounce": só busca 300 ms depois que a pessoa para de digitar
  useEffect(() => {
    const t = setTimeout(() => setFiltros((f) => ({ ...f, busca })), 300);
    return () => clearTimeout(t);
  }, [busca]);

  const notificar = (msg) => { setAviso(msg); setTimeout(() => setAviso(''), 3000); };

  function salvo(msg) { setEditando(null); notificar(msg); carregar(); }

  async function confirmarExclusao() {
    try {
      await api.veiculos.excluir(excluindo.id);
      setExcluindo(null);
      notificar('Veículo excluído.');
      carregar();
    } catch (e) {
      setErroExclusao(e.message);
    }
  }

  const abrirExclusao = (v) => { setErroExclusao(''); setExcluindo(v); };

  return (
    <div className="card">
      <div className="barra-ferramentas">
        <div className="filtros">
          <Icone nome="filtro" />
          <input className="busca" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Buscar placa ou modelo" />
          <div className="segmentado">
            {[['', 'Todos'], ...Object.entries(TIPOS_VEICULO)].map(([k, r]) => (
              <button key={k} className={filtros.tipo === k ? 'ativo' : ''}
                onClick={() => setFiltros((f) => ({ ...f, tipo: k }))}>{r}</button>
            ))}
          </div>
        </div>
        <button className="btn primario" onClick={() => setEditando({})}>
          <Icone nome="mais" /> Cadastrar veículo
        </button>
      </div>

      {!lista ? <Carregando /> : lista.length === 0 ? (
        <div className="vazio">Nenhum veículo encontrado.</div>
      ) : (
        <div className="tabela-scroll">
          <table className="tabela">
            <thead>
              <tr>
                <th>Placa</th><th>Modelo</th><th>Tipo</th><th className="dir">Ano</th>
                <th className="dir">Viagens</th><th className="dir">Manutenções</th><th />
              </tr>
            </thead>
            <tbody>
              {lista.map((v) => (
                <tr key={v.id}>
                  <td>
                    <div className="celula-veiculo">
                      <span className={`tag-tipo ${v.tipo === 'PESADO' ? 'pesado' : 'leve'}`}>{v.tipo === 'PESADO' ? 'P' : 'L'}</span>
                      <strong>{v.placa}</strong>
                    </div>
                  </td>
                  <td>{v.modelo}</td>
                  <td>{TIPOS_VEICULO[v.tipo]}</td>
                  <td className="dir num">{v.ano ?? '—'}</td>
                  <td className="dir num">{v.viagens}</td>
                  <td className="dir num">{v.manutencoes}</td>
                  <td className="acoes">
                    <button className="btn-icone" title="Editar" onClick={() => setEditando(v)}><Icone nome="editar" /></button>
                    <button className="btn-icone perigo" title={v.podeExcluir ? 'Excluir' : 'Possui histórico — não pode ser excluído'}
                      disabled={!v.podeExcluir} onClick={() => abrirExclusao(v)}><Icone nome="lixeira" /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {lista && <div className="rodape-tabela">{lista.length} veículo{lista.length === 1 ? '' : 's'}</div>}

      {editando && (
        <Modal titulo={editando.id ? `Editar veículo ${editando.placa}` : 'Cadastrar veículo'} onFechar={() => setEditando(null)}>
          <VeiculoForm inicial={editando} onCancelar={() => setEditando(null)}
            onSalvo={() => salvo(editando.id ? 'Veículo atualizado.' : 'Veículo cadastrado.')} />
        </Modal>
      )}

      {excluindo && (
        <Modal titulo="Excluir veículo" largura={420} onFechar={() => setExcluindo(null)}>
          <p className="modal-texto">
            Excluir o veículo <strong>{excluindo.placa}</strong> ({excluindo.modelo})? Essa ação não pode ser desfeita.
          </p>
          {erroExclusao && <div className="alerta erro" style={{ margin: '12px 22px 0' }}>{erroExclusao}</div>}
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
