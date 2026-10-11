import { useState } from 'react';
import { api } from '../api.js';
import { Icone } from '../components/Icone.jsx';

export default function Login({ onEntrar }) {
  const [email, setEmail] = useState('admin@logitrack.com');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);

  async function entrar(e) {
    e.preventDefault();
    setErro('');
    setEnviando(true);
    try {
      onEntrar(await api.login(email, senha));
    } catch (err) {
      setErro(err.status === 401 ? 'E-mail ou senha inválidos.' : 'Não foi possível conectar à API.');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="login">
      <section className="login-arte">
        <div className="marca grande">
          <span className="marca-logo"><Icone nome="caminhao" tamanho={26} /></span>
          <div><strong>LogiTrack</strong><small>PRO</small></div>
        </div>
        <div>
          <h1>Sua frota sob controle,<br />do agendamento ao custo.</h1>
          <p>Centralize manutenções, acompanhe a quilometragem e antecipe os gastos do mês em um só lugar.</p>
        </div>
        <ul className="login-destaques">
          <li><Icone nome="calendario" /> Agenda sem conflitos por veículo</li>
          <li><Icone nome="grafico" /> Indicadores calculados direto no banco</li>
          <li><Icone nome="dinheiro" /> Projeção de custos do mês</li>
        </ul>
      </section>

      <section className="login-form-area">
        <form className="login-form" onSubmit={entrar}>
          <h2>Entrar</h2>
          <p className="subtitulo">Acesse com sua conta de gestor.</p>

          <label>E-mail
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
          </label>
          <label>Senha
            <input type="password" value={senha} onChange={(e) => setSenha(e.target.value)} required placeholder="••••••••" />
          </label>

          {erro && <div className="alerta erro">{erro}</div>}

          <button className="btn primario largo" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>
          <p className="dica">Demonstração: admin@logitrack.com / admin123</p>
        </form>
      </section>
    </div>
  );
}
