package br.com.logitrack.service;

import br.com.logitrack.domain.Manutencao;
import br.com.logitrack.domain.StatusManutencao;
import br.com.logitrack.domain.Veiculo;
import br.com.logitrack.dto.ManutencaoRequest;
import br.com.logitrack.dto.ManutencaoResponse;
import br.com.logitrack.exception.RecursoNaoEncontradoException;
import br.com.logitrack.exception.RegraNegocioException;
import br.com.logitrack.repository.ManutencaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ManutencaoService {

    private final ManutencaoRepository manutencoes;
    private final VeiculoService veiculos;

    public ManutencaoService(ManutencaoRepository manutencoes, VeiculoService veiculos) {
        this.manutencoes = manutencoes;
        this.veiculos = veiculos;
    }

    @Transactional(readOnly = true)
    public Page<ManutencaoResponse> listar(StatusManutencao status, Integer veiculoId, Pageable pageable) {
        return manutencoes.buscar(status, veiculoId, pageable).map(ManutencaoResponse::de);
    }

    @Transactional(readOnly = true)
    public ManutencaoResponse buscar(Integer id) {
        return ManutencaoResponse.de(obter(id));
    }

    /**
     * Agenda uma manutenção. Se vier um veículo novo, ele é cadastrado na MESMA transação:
     * se a manutenção for recusada (ex.: datas inválidas), o veículo também não é salvo.
     */
    @Transactional
    public ManutencaoResponse criar(ManutencaoRequest req) {
        Veiculo veiculo = resolverVeiculo(req, null);
        Manutencao nova = new Manutencao(veiculo, req.dataInicio(), req.dataFinalizacao(),
                req.tipoServico(), req.custoEstimado(), req.status());
        return ManutencaoResponse.de(manutencoes.save(nova));
    }

    @Transactional
    public ManutencaoResponse atualizar(Integer id, ManutencaoRequest req) {
        Manutencao existente = obter(id);
        Veiculo veiculo = resolverVeiculo(req, id);
        existente.atualizar(veiculo, req.dataInicio(), req.dataFinalizacao(),
                req.tipoServico(), req.custoEstimado(), req.status());
        return ManutencaoResponse.de(existente);
    }

    @Transactional
    public ManutencaoResponse alterarStatus(Integer id, StatusManutencao status) {
        Manutencao existente = obter(id);
        existente.alterarStatus(status);
        return ManutencaoResponse.de(existente);
    }

    @Transactional
    public void excluir(Integer id) {
        manutencoes.delete(obter(id));
    }

    /**
     * Veículo novo -> cadastra (não pode haver conflito de agenda, ele acabou de nascer).
     * Veículo existente -> busca e verifica conflito de agenda.
     */
    private Veiculo resolverVeiculo(ManutencaoRequest req, Integer ignorarManutencaoId) {
        if (req.novoVeiculo() != null) {
            return veiculos.cadastrar(req.novoVeiculo());
        }
        Veiculo veiculo = veiculos.obter(req.veiculoId());
        validarConflito(veiculo.getId(), req, ignorarManutencaoId);
        return veiculo;
    }

    private void validarConflito(Integer veiculoId, ManutencaoRequest req, Integer ignorarId) {
        if (req.status() == StatusManutencao.CONCLUIDA) {
            return; // registros históricos não bloqueiam a agenda
        }
        LocalDate fim = req.dataFinalizacao() != null ? req.dataFinalizacao() : req.dataInicio();
        if (manutencoes.existeConflito(veiculoId, req.dataInicio(), fim, ignorarId)) {
            throw new RegraNegocioException(
                    "Este veículo já possui uma manutenção agendada que se sobrepõe a esse período.");
        }
    }

    private Manutencao obter(Integer id) {
        return manutencoes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Manutenção", id));
    }
}
