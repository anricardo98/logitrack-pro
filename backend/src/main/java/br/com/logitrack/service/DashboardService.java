package br.com.logitrack.service;

import br.com.logitrack.dto.DashboardDtos.*;
import br.com.logitrack.repository.DashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int LIMITE_CRONOGRAMA = 5;
    private static final int LIMITE_RANKING = 5;

    private final DashboardRepository repo;
    private final Clock clock;

    public DashboardService(DashboardRepository repo, Clock clock) {
        this.repo = repo;
        this.clock = clock;
    }

    public TotalKm totalKm(Integer veiculoId) {
        return new TotalKm(veiculoId, repo.totalKm(veiculoId));
    }

    public List<VolumeCategoria> volumePorCategoria(String tipo) {
        return repo.volumePorCategoria(tipo);
    }

    public List<ProximaManutencao> cronograma() {
        return repo.proximasManutencoes(LocalDate.now(clock), LIMITE_CRONOGRAMA);
    }

    public List<RankingVeiculo> ranking() {
        return repo.rankingUtilizacao(LIMITE_RANKING);
    }

    public ProjecaoFinanceira projecaoMesAtual() {
        return repo.projecaoMes(LocalDate.now(clock));
    }

    /** Tudo em uma chamada — evita 5 requisições ao abrir o dashboard. */
    public Resumo resumo(Integer veiculoId) {
        return new Resumo(totalKm(veiculoId), volumePorCategoria(null), cronograma(), ranking(), projecaoMesAtual());
    }
}
