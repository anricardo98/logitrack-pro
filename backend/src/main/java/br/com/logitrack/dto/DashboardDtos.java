package br.com.logitrack.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() { }

    public record TotalKm(Integer veiculoId, BigDecimal totalKm) { }

    public record VolumeCategoria(String tipo, long quantidadeViagens) { }

    public record ProximaManutencao(Integer id, String placa, String modelo, LocalDate dataInicio,
                                    LocalDate dataFinalizacao, String tipoServico,
                                    BigDecimal custoEstimado, String status) { }

    public record RankingVeiculo(Integer veiculoId, String placa, String modelo, String tipo,
                                 BigDecimal totalKm, long quantidadeViagens) { }

    public record ProjecaoFinanceira(int ano, int mes, BigDecimal custoTotalEstimado, long quantidadeManutencoes) { }

    public record Resumo(TotalKm totalKm, List<VolumeCategoria> volumePorCategoria,
                         List<ProximaManutencao> cronograma, List<RankingVeiculo> ranking,
                         ProjecaoFinanceira projecaoMesAtual) { }
}
