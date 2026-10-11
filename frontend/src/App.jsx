import { useEffect, useState } from 'react';
import { sessao } from './api.js';
import Sidebar from './components/Sidebar.jsx';
import Login from './pages/Login.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Manutencoes from './pages/Manutencoes.jsx';
import Veiculos from './pages/Veiculos.jsx';

const PAGINAS = {
  dashboard: { titulo: 'Dashboard', subtitulo: 'Atualizações da frota em tempo real', Componente: Dashboard },
  manutencoes: { titulo: 'Manutenções', subtitulo: 'Agendamento e acompanhamento dos serviços', Componente: Manutencoes },
  veiculos: { titulo: 'Veículos', subtitulo: 'Cadastro da frota', Componente: Veiculos },
};

const paginaDaUrl = () => (location.hash.replace('#/', '') in PAGINAS ? location.hash.replace('#/', '') : 'dashboard');

export default function App() {
  const [usuario, setUsuario] = useState(sessao.obter());
  const [pagina, setPagina] = useState(paginaDaUrl);

  useEffect(() => {
    const sair = () => setUsuario(null);
    const navegar = () => setPagina(paginaDaUrl());
    window.addEventListener('logitrack:logout', sair);
    window.addEventListener('hashchange', navegar);
    return () => {
      window.removeEventListener('logitrack:logout', sair);
      window.removeEventListener('hashchange', navegar);
    };
  }, []);

  if (!usuario) {
    return <Login onEntrar={(dados) => { sessao.salvar(dados); setUsuario(dados); }} />;
  }

  const { titulo, subtitulo, Componente } = PAGINAS[pagina];
  const sair = () => { sessao.limpar(); setUsuario(null); };

  return (
    <div className="layout">
      <Sidebar ativa={pagina} usuario={usuario} onSair={sair} />
      <main className="conteudo">
        <header className="topo">
          <div>
            <h1>{titulo}</h1>
            <p className="subtitulo">{subtitulo}</p>
          
          </div>
        </header>
        <Componente />
      </main>
    </div>
  );
}
