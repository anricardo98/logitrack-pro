import { TIPOS_VEICULO } from '../format.js';

export const VEICULO_VAZIO = { placa: '', modelo: '', tipo: '', ano: '' };

const PLACA = /^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$/i;
const ANO_MAX = new Date().getFullYear() + 1;

export function validarVeiculo(v) {
  const e = {};
  if (!v.placa.trim()) e.placa = 'Informe a placa';
  else if (!PLACA.test(v.placa.trim())) e.placa = 'Placa inválida (ex.: ABC-1234 ou ABC1D23)';
  if (!v.modelo.trim()) e.modelo = 'Informe o modelo';
  if (!v.tipo) e.tipo = 'Selecione o tipo';
  if (v.ano !== '' && (Number(v.ano) < 1950 || Number(v.ano) > ANO_MAX)) e.ano = `Entre 1950 e ${ANO_MAX}`;
  return e;
}

export const paraApi = (v) => ({
  placa: v.placa.trim().toUpperCase(),
  modelo: v.modelo.trim(),
  tipo: v.tipo,
  ano: v.ano === '' ? null : Number(v.ano),
});

export default function VeiculoCampos({ valor, erros = {}, onChange }) {
  const alterar = (campo) => (e) => onChange({ ...valor, [campo]: e.target.value }, campo);
  const Erro = ({ campo }) => (erros[campo] ? <span className="erro-campo">{erros[campo]}</span> : null);

  return (
    <>
      <label>Placa
        <input value={valor.placa} onChange={alterar('placa')} maxLength={8} placeholder="ABC-1234"
          style={{ textTransform: 'uppercase' }} className={erros.placa ? 'invalido' : ''} />
        <Erro campo="placa" />
      </label>
      <label>Modelo
        <input value={valor.modelo} onChange={alterar('modelo')} maxLength={50} placeholder="Ex.: Volvo FH"
          className={erros.modelo ? 'invalido' : ''} />
        <Erro campo="modelo" />
      </label>
      <label>Tipo
        <div className={`segmentado largo ${erros.tipo ? 'invalido' : ''}`} role="radiogroup">
          {Object.entries(TIPOS_VEICULO).map(([k, r]) => (
            <button type="button" key={k} role="radio" aria-checked={valor.tipo === k}
              className={valor.tipo === k ? 'ativo' : ''} onClick={() => onChange({ ...valor, tipo: k }, 'tipo')}>{r}</button>
          ))}
        </div>
        <Erro campo="tipo" />
      </label>
      <label>Ano
        <input type="number" value={valor.ano} onChange={alterar('ano')} min="1950" max={ANO_MAX} placeholder="2024"
          className={erros.ano ? 'invalido' : ''} />
        <Erro campo="ano" />
      </label>
    </>
  );
}
