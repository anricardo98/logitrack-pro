import { Icone } from './Icone.jsx';

const ITENS = [
  { id: 'dashboard', rotulo: 'Dashboard', icone: 'grafico' },
  { id: 'manutencoes', rotulo: 'Manutenções', icone: 'chave' },
  { id: 'veiculos', rotulo: 'Veículos', icone: 'caminhao' },
];

export default function Sidebar({ ativa, usuario, onSair }) {
  const iniciais = usuario.nome.split(' ').map((p) => p[0]).slice(0, 2).join('');
  return (
    <aside className="sidebar">
      <div className="marca">
        <span className="marca-logo"><Icone nome="caminhao" /></span>
        <div>
          <strong>LogiTrack</strong>
          <small>PRO</small>
        </div>
      </div>

      <nav>
        <span className="nav-secao">Menu</span>
        {ITENS.map((item) => (
          <a key={item.id} href={`#/${item.id}`} className={ativa === item.id ? 'ativo' : ''} title={item.rotulo}>
            <Icone nome={item.icone} /> <span className="nav-rotulo">{item.rotulo}</span>
          </a>
        ))}
      </nav>

      <div className="usuario">
        <span className="avatar">{iniciais}</span>
        <div className="usuario-info">
          <strong>{usuario.nome}</strong>
          <small>{usuario.email}</small>
        </div>
        <button className="btn-icone" title="Sair" onClick={onSair}><Icone nome="sair" /></button>
      </div>
    </aside>
  );
}
