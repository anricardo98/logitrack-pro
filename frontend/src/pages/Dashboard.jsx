import { useEffect, useState } from 'react';
import { api } from '../api.js';
import { Carregando, Kpi, StatusBadge } from '../components/Ui.jsx';
import { BarrasHorizontais, Rosca } from '../components/Graficos.jsx';
import { diasAte, formatarData, formatarKm, formatarMoeda, nomeMes } from '../format.js';

export default function Dashboard() {
  const [resumo, setResumo] = useState(null);
  const [veiculos, setVeiculos] = useState([]);
  const [veiculoKm, setVeiculoKm] = useState('');
  const [totalKm, setTotalKm] = useState(null);
  const [erro, setErro] = useState('');

  useEffect(() => {
    Promise.all([api.dashboard.resumo(), api.veiculos.listar()])
      .then(([r, v]) => { setResumo(r); setTotalKm(r.totalKm.totalKm); setVeiculos(v); })
      .catch(() => setErro('Não foi possível carregar o dashboard.'));
  }, []);

  async function trocarVeiculo(id) {
    setVeiculoKm(id);
    const r = await api.dashboard.totalKm(id || undefined);
    setTotalKm(r.totalKm);
  }

  if (erro) return <div className="alerta erro">{erro}</div>;
  if (!resumo) return <Carregando />;

  const { volumePorCategoria, cronograma, ranking, projecaoMesAtual: proj } = resumo;
  const lider = ranking[0];
  const totalViagens = volumePorCategoria.reduce((s, d) => s + d.quantidadeViagens, 0);

  return (
    <div className="dashboard">
      <section className="grid-kpis">
        <Kpi icone="rota" cor="azul" rotulo="Total de KM percorrido" valor={formatarKm(totalKm)}>
          <select className="select-mini" value={veiculoKm} onChange={(e) => trocarVeiculo(e.target.value)}>
            <option value="">Toda a frota</option>
            {veiculos.map((v) => <option key={v.id} value={v.id}>{v.placa} · {v.modelo}</option>)}
          </select>
        </Kpi>

        <Kpi icone="dinheiro" cor="verde" rotulo={`Projeção de ${nomeMes(proj.mes)}`}
          valor={formatarMoeda(proj.custoTotalEstimado)}
          detalhe={`${proj.quantidadeManutencoes} manutenç${proj.quantidadeManutencoes === 1 ? 'ão' : 'ões'} no mês`} />

        <Kpi icone="trofeu" cor="laranja" rotulo="Veículo mais utilizado"
          valor={lider ? lider.placa : '—'}
          detalhe={lider ? `${lider.modelo} · ${formatarKm(lider.totalKm)}` : 'Sem viagens'} />

        <Kpi icone="camadas" cor="roxo" rotulo="Viagens realizadas" valor={totalViagens}
          detalhe={volumePorCategoria.map((d) => `${d.quantidadeViagens} ${d.tipo === 'LEVE' ? 'leves' : 'pesados'}`).join(' · ')} />
      </section>

      <section className="grid-graficos">
        <div className="card">
          <div className="card-cabecalho">
            <h3>Ranking de utilização</h3>
            <span className="legenda-inline"><i className="leve" /> Leve <i className="pesado" /> Pesado</span>
          </div>
          <BarrasHorizontais dados={ranking} />
        </div>

        <div className="card">
          <div className="card-cabecalho"><h3>Volume por categoria</h3></div>
          <Rosca dados={volumePorCategoria} />
        </div>
      </section>

      <section className="card">
        <div className="card-cabecalho">
          <h3>Cronograma de manutenção</h3>
          <a href="#/manutencoes" className="link">Ver todas →</a>
        </div>
        {cronograma.length === 0 ? (
          <div className="vazio">Nenhuma manutenção agendada.</div>
        ) : (
          <ol className="timeline">
            {cronograma.map((m) => {
              const dias = diasAte(m.dataInicio);
              const [, mes, dia] = m.dataInicio.split('-');
              return (
                <li key={m.id}>
                  <div className="timeline-data"><strong>{dia}</strong><small>{nomeMes(Number(mes)).slice(0, 3)}</small></div>
                  <div className="timeline-corpo">
                    <strong>{m.tipoServico}</strong>
                    <small>{m.placa} · {m.modelo} · até {formatarData(m.dataFinalizacao)}</small>
                  </div>
                  <span className="timeline-prazo">{dias === 0 ? 'hoje' : dias === 1 ? 'amanhã' : `em ${dias} dias`}</span>
                  <span className="timeline-custo">{formatarMoeda(m.custoEstimado)}</span>
                  <StatusBadge status={m.status} />
                </li>
              );
            })}
          </ol>
        )}
      </section>
    </div>
  );
}
