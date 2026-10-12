import { useState } from 'react';
import { api } from '../api.js';
import VeiculoCampos, { VEICULO_VAZIO, paraApi, validarVeiculo } from '../components/VeiculoCampos.jsx';

export default function VeiculoForm({ inicial, onSalvo, onCancelar }) {
  const [form, setForm] = useState(() => (inicial.id
    ? { placa: inicial.placa, modelo: inicial.modelo, tipo: inicial.tipo, ano: inicial.ano ?? '' }
    : VEICULO_VAZIO));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState('');
  const [salvando, setSalvando] = useState(false);

  function alterar(novo, campo) {
    setForm(novo);
    setErros((e) => ({ ...e, [campo]: undefined }));
  }

  async function enviar(ev) {
    ev.preventDefault();
    setErroGeral('');
    const e = validarVeiculo(form);
    setErros(e);
    if (Object.keys(e).length) return;

    setSalvando(true);
    try {
      if (inicial.id) await api.veiculos.atualizar(inicial.id, paraApi(form));
      else await api.veiculos.criar(paraApi(form));
      onSalvo();
    } catch (err) {
      setErros(err.campos ?? {});
      setErroGeral(err.message);
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form className="form" onSubmit={enviar} noValidate>
      <VeiculoCampos valor={form} erros={erros} onChange={alterar} />
      {erroGeral && <div className="alerta erro col-2">{erroGeral}</div>}
      <div className="modal-acoes col-2">
        <button type="button" className="btn" onClick={onCancelar}>Cancelar</button>
        <button className="btn primario" disabled={salvando}>{salvando ? 'Salvando…' : 'Salvar'}</button>
      </div>
    </form>
  );
}
