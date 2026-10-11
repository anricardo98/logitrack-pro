package br.com.logitrack.repository;

import br.com.logitrack.dto.DashboardDtos.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

/**
 * As 5 métricas do dashboard, extraídas diretamente via SQL nativo
 * (requisito do desafio). Toda agregação é feita no banco — a aplicação
 * só recebe o resultado pronto.
 */
@Repository
public class DashboardRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public DashboardRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 1. Total de KM percorrido — de um veículo específico ou de toda a frota (veiculoId nulo). */
    public BigDecimal totalKm(Integer veiculoId) {
        String sql = """
                SELECT COALESCE(SUM(km_percorrida), 0)
                FROM viagens
                WHERE CAST(:veiculoId AS INTEGER) IS NULL OR veiculo_id = CAST(:veiculoId AS INTEGER)
                """;
        return jdbc.queryForObject(sql, new MapSqlParameterSource().addValue("veiculoId", veiculoId, Types.INTEGER), BigDecimal.class);
    }

    /** 2. Volume por categoria — quantidade de viagens por tipo de veículo (LEVE x PESADO). */
    public List<VolumeCategoria> volumePorCategoria(String tipo) {
        String sql = """
                SELECT v.tipo, COUNT(vi.id) AS quantidade
                FROM veiculos v
                LEFT JOIN viagens vi ON vi.veiculo_id = v.id
                WHERE CAST(:tipo AS VARCHAR) IS NULL OR v.tipo = CAST(:tipo AS VARCHAR)
                GROUP BY v.tipo
                ORDER BY v.tipo
                """;
        return jdbc.query(sql, new MapSqlParameterSource().addValue("tipo", tipo, Types.VARCHAR),
                (rs, i) -> new VolumeCategoria(rs.getString("tipo"), rs.getLong("quantidade")));
    }

    /** 3. Cronograma — as próximas 5 manutenções agendadas, ordenadas por data. */
    public List<ProximaManutencao> proximasManutencoes(LocalDate hoje, int limite) {
        String sql = """
                SELECT m.id, v.placa, v.modelo, m.data_inicio, m.data_finalizacao,
                       m.tipo_servico, m.custo_estimado, m.status
                FROM manutencoes m
                JOIN veiculos v ON v.id = m.veiculo_id
                WHERE m.status <> 'CONCLUIDA'
                  AND m.data_inicio >= :hoje
                ORDER BY m.data_inicio, m.id
                LIMIT :limite
                """;
        var params = new MapSqlParameterSource("hoje", hoje).addValue("limite", limite);
        return jdbc.query(sql, params, (rs, i) -> new ProximaManutencao(
                rs.getInt("id"), rs.getString("placa"), rs.getString("modelo"),
                rs.getObject("data_inicio", LocalDate.class),
                rs.getObject("data_finalizacao", LocalDate.class),
                rs.getString("tipo_servico"), rs.getBigDecimal("custo_estimado"), rs.getString("status")));
    }

    /**
     * 4. Ranking de utilização — veículos ordenados pela soma de KM.
     * O primeiro da lista é o veículo de maior utilização; os demais alimentam o gráfico.
     */
    public List<RankingVeiculo> rankingUtilizacao(int limite) {
        String sql = """
                SELECT v.id, v.placa, v.modelo, v.tipo,
                       COALESCE(SUM(vi.km_percorrida), 0) AS total_km,
                       COUNT(vi.id)                       AS viagens
                FROM veiculos v
                LEFT JOIN viagens vi ON vi.veiculo_id = v.id
                GROUP BY v.id, v.placa, v.modelo, v.tipo
                ORDER BY total_km DESC, v.placa
                LIMIT :limite
                """;
        return jdbc.query(sql, new MapSqlParameterSource("limite", limite), (rs, i) -> new RankingVeiculo(
                rs.getInt("id"), rs.getString("placa"), rs.getString("modelo"), rs.getString("tipo"),
                rs.getBigDecimal("total_km"), rs.getLong("viagens")));
    }

    /**
     * 5. Projeção financeira — soma do custo estimado das manutenções do mês atual.
     * Usa intervalo [1º dia do mês, 1º dia do mês seguinte) em vez de EXTRACT(MONTH ...)
     * para que o índice em data_inicio possa ser aproveitado.
     */
    public ProjecaoFinanceira projecaoMes(LocalDate referencia) {
        LocalDate inicio = referencia.withDayOfMonth(1);
        LocalDate fim = inicio.plusMonths(1);
        String sql = """
                SELECT COALESCE(SUM(custo_estimado), 0) AS total, COUNT(*) AS quantidade
                FROM manutencoes
                WHERE data_inicio >= :inicio AND data_inicio < :fim
                """;
        var params = new MapSqlParameterSource("inicio", inicio).addValue("fim", fim);
        return jdbc.queryForObject(sql, params, (rs, i) -> new ProjecaoFinanceira(
                inicio.getYear(), inicio.getMonthValue(), rs.getBigDecimal("total"), rs.getLong("quantidade")));
    }
}
