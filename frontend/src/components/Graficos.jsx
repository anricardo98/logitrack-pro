import { formatarKm, formatarNumero } from '../format.js';

export function BarrasHorizontais({ dados }) {
  const max = Math.max(...dados.map((d) => Number(d.totalKm)), 1);
  return (
    <ul className="barras">
      {dados.map((d, i) => (
        <li key={d.veiculoId}>
          <div className="barras-rotulo">
            <span><b className={`posicao ${i === 0 ? 'ouro' : ''}`}>{i + 1}</b> {d.placa} <small>{d.modelo}</small></span>
            <strong>{formatarKm(d.totalKm)}</strong>
          </div>
          <div className="barras-trilho">
            <div className={`barras-preenchimento ${d.tipo === 'PESADO' ? 'pesado' : 'leve'}`}
              style={{ width: `${(Number(d.totalKm) / max) * 100}%` }} />
          </div>
        </li>
      ))}
    </ul>
  );
}

export function Rosca({ dados }) {
  const total = dados.reduce((s, d) => s + d.quantidadeViagens, 0);
  const r = 54;
  const circ = 2 * Math.PI * r;
  let acumulado = 0;
  const cores = { LEVE: 'var(--leve)', PESADO: 'var(--pesado)' };

  return (
    <div className="rosca">
      <svg viewBox="0 0 140 140" width="150" height="150">
        <circle cx="70" cy="70" r={r} fill="none" stroke="var(--trilho)" strokeWidth="16" />
        {total > 0 && dados.map((d) => {
          const fatia = (d.quantidadeViagens / total) * circ;
          const el = (
            <circle key={d.tipo} cx="70" cy="70" r={r} fill="none" stroke={cores[d.tipo]} strokeWidth="16"
              strokeDasharray={`${fatia} ${circ - fatia}`} strokeDashoffset={-acumulado}
              transform="rotate(-90 70 70)" />
          );
          acumulado += fatia;
          return el;
        })}
        <text x="70" y="66" textAnchor="middle" className="rosca-total">{total}</text>
        <text x="70" y="86" textAnchor="middle" className="rosca-legenda">viagens</text>
      </svg>
      <ul className="legenda">
        {dados.map((d) => (
          <li key={d.tipo}>
            <span className="ponto" style={{ background: cores[d.tipo] }} />
            <span>{d.tipo === 'LEVE' ? 'Leve' : 'Pesado'}</span>
            <strong>{formatarNumero(d.quantidadeViagens)}</strong>
            <small>{total ? Math.round((d.quantidadeViagens / total) * 100) : 0}%</small>
          </li>
        ))}
      </ul>
    </div>
  );
}
