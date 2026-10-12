import { useState } from 'react';
import { api } from '../api.js';
import { STATUS, TIPOS_SERVICO } from '../format.js';
import VeiculoCampos, { VEICULO_VAZIO, paraApi, validarVeiculo } from '../components/VeiculoCampos.jsx';

const VAZIO = { veiculoId: '', dataInicio: '', dataFinalizacao: '', tipoServico: '', custoEstimado: '', status: 'PENDENTE' };

function paraFormulario(m) {
  if (!m.id) return VAZIO;
  return {
    veiculoId: String(m.veiculo.id),
    dataInicio: m.dataInicio,
    dataFinalizacao: m.dataFinalizacao ?? '',
    tipoServico: m.tipoServico,
    custoEstimado: m.custoEstimado ?? '',
    status: m.status,
  };
}

/** Erros do backend para o veículo novo chegam como "novoVeiculo.placa" → viram { placa: … }. */
function separarErros(campos = {}) {
  const manutencao = {}, veiculo = {};
  Object.entries(campos).forEach(([k, msg]) => {
    if (k.startsWith('novoVeiculo.')) veiculo[k.slice('novoVeiculo.'.length)] = msg;
    else if (k === 'veiculoInformado') manutencao.veiculoId = msg;
    else manutencao[k] = msg;
  });
  return { manutencao, veiculo };
}

export default function ManutencaoForm({ inicial, veiculos, onSalvo, onCancelar }) {
  const [form, setForm] = useState(() => paraFormulario(inicial));
  const [modoVeiculo, setModoVeiculo] = useState('existente');
  const [novoVeiculo, setNovoVeiculo] = useState(VEICULO_VAZIO);
  const [erros, setErros] = useState({});
  const [errosVeiculo, setErrosVeiculo] = useState({});
  const [erroGeral, setErroGeral] = useState('');
  const [salvando, setSalvando] = useState(false);

  const alterar = (campo) => (e) => {
    setForm((f) => ({ ...f, [campo]: e.target.value }));
    setErros((er) => ({ ...er, [campo]: undefined }));
  };

  function alterarVeiculo(novo, campo) {
    setNovoVeiculo(novo);
    setErrosVeiculo((er) => ({ ...er, [campo]: undefined }));
  }

  function validar() {
    const e = {};
    if (modoVeiculo === 'existente' && !form.veiculoId) e.veiculoId = 'Selecione o veículo';
    if (!form.dataInicio) e.dataInicio = 'Informe a data de início';
    if (form.dataFinalizacao && form.dataInicio && form.dataFinalizacao < form.dataInicio)
      e.dataFinalizacao = 'Deve ser igual ou posterior ao início';
    if (!form.tipoServico.trim()) e.tipoServico = 'Informe o tipo de serviço';
    if (form.custoEstimado !== '' && Number(form.custoEstimado) < 0) e.custoEstimado = 'O custo não pode ser negativo';
    if (form.status === 'CONCLUIDA' && !form.dataFinalizacao) e.dataFinalizacao = 'Obrigatória para concluir';
    const ev = modoVeiculo === 'novo' ? validarVeiculo(novoVeiculo) : {};
    setErros(e);
    setErrosVeiculo(ev);
    return Object.keys(e).length + Object.keys(ev).length === 0;
  }

  async function enviar(ev) {
    ev.preventDefault();
    setErroGeral('');
    if (!validar()) return;
    const corpo = {
      veiculoId: modoVeiculo === 'existente' ? Number(form.veiculoId) : null,
      novoVeiculo: modoVeiculo === 'novo' ? paraApi(novoVeiculo) : null,
      dataInicio: form.dataInicio,
      dataFinalizacao: form.dataFinalizacao || null,
      tipoServico: form.tipoServico.trim(),
      custoEstimado: form.custoEstimado === '' ? null : Number(form.custoEstimado),
      status: form.status,
    };
    setSalvando(true);
    try {
      if (inicial.id) await api.manutencoes.atualizar(inicial.id, corpo);
      else await api.manutencoes.criar(corpo);
      onSalvo({ veiculoNovo: modoVeiculo === 'novo' });
    } catch (err) {
      const { manutencao, veiculo } = separarErros(err.campos);
      setErros(manutencao);
      setErrosVeiculo(veiculo);
      setErroGeral(err.message);
    } finally {
      setSalvando(false);
    }
  }

  const Erro = ({ campo }) => (erros[campo] ? <span className="erro-campo">{erros[campo]}</span> : null);

  return (
    <form className="form" onSubmit={enviar} noValidate>
      <div className="col-2 campo-veiculo">
        <div className="campo-veiculo-topo">
          <span className="rotulo">Veículo</span>
          <div className="segmentado">
            <button type="button" className={modoVeiculo === 'existente' ? 'ativo' : ''}
              onClick={() => setModoVeiculo('existente')}>Já cadastrado</button>
            <button type="button" className={modoVeiculo === 'novo' ? 'ativo' : ''}
              onClick={() => setModoVeiculo('novo')}>+ Novo veículo</button>
          </div>
        </div>

        {modoVeiculo === 'existente' ? (
          <>
            <select value={form.veiculoId} onChange={alterar('veiculoId')} className={erros.veiculoId ? 'invalido' : ''}>
              <option value="">Selecione…</option>
              {veiculos.map((v) => (
                <option key={v.id} value={v.id}>{v.placa} · {v.modelo} ({v.tipo === 'PESADO' ? 'Pesado' : 'Leve'})</option>
              ))}
            </select>
            <Erro campo="veiculoId" />
          </>
        ) : (
          <div className="subform">
            <p className="subform-dica">O veículo é cadastrado junto com a manutenção. Se o agendamento falhar, nada é salvo.</p>
            <div className="form-grade">
              <VeiculoCampos valor={novoVeiculo} erros={errosVeiculo} onChange={alterarVeiculo} />
            </div>
          </div>
        )}
      </div>

      <label>Data de início
        <input type="date" value={form.dataInicio} onChange={alterar('dataInicio')} className={erros.dataInicio ? 'invalido' : ''} />
        <Erro campo="dataInicio" />
      </label>
      <label>Finalização prevista
        <input type="date" value={form.dataFinalizacao} min={form.dataInicio || undefined}
          onChange={alterar('dataFinalizacao')} className={erros.dataFinalizacao ? 'invalido' : ''} />
        <Erro campo="dataFinalizacao" />
      </label>

      <label className="col-2">Tipo de serviço
        <input list="tipos-servico" value={form.tipoServico} onChange={alterar('tipoServico')} maxLength={100}
          placeholder="Ex.: Troca de Óleo" className={erros.tipoServico ? 'invalido' : ''} />
        <datalist id="tipos-servico">{TIPOS_SERVICO.map((t) => <option key={t} value={t} />)}</datalist>
        <Erro campo="tipoServico" />
      </label>

      <label>Custo estimado (R$)
        <input type="number" min="0" step="0.01" value={form.custoEstimado} onChange={alterar('custoEstimado')}
          placeholder="0,00" className={erros.custoEstimado ? 'invalido' : ''} />
        <Erro campo="custoEstimado" />
      </label>
      <label>Status
        <select value={form.status} onChange={alterar('status')}>
          {Object.entries(STATUS).map(([k, v]) => <option key={k} value={k}>{v.rotulo}</option>)}
        </select>
      </label>

      {erroGeral && <div className="alerta erro col-2">{erroGeral}</div>}

      <div className="modal-acoes col-2">
        <button type="button" className="btn" onClick={onCancelar}>Cancelar</button>
        <button className="btn primario" disabled={salvando}>{salvando ? 'Salvando…' : 'Salvar'}</button>
      </div>
    </form>
  );
}
