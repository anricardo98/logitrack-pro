package br.com.logitrack.repository;

import br.com.logitrack.domain.Manutencao;
import br.com.logitrack.domain.StatusManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Integer> {

    /** EntityGraph carrega o veículo na mesma consulta (evita N+1). */
    @EntityGraph(attributePaths = "veiculo")
    @Query("""
           SELECT m FROM Manutencao m
           WHERE (:status IS NULL OR m.status = :status)
             AND (:veiculoId IS NULL OR m.veiculo.id = :veiculoId)
           """)
    Page<Manutencao> buscar(@Param("status") StatusManutencao status,
                            @Param("veiculoId") Integer veiculoId,
                            Pageable pageable);

    /** Períodos sobrepostos do mesmo veículo. Sem data de finalização, a manutenção conta como de um dia. */
    @Query("""
           SELECT COUNT(m) > 0 FROM Manutencao m
           WHERE m.veiculo.id = :veiculoId
             AND m.status <> br.com.logitrack.domain.StatusManutencao.CONCLUIDA
             AND (:ignorarId IS NULL OR m.id <> :ignorarId)
             AND m.dataInicio <= :fim
             AND COALESCE(m.dataFinalizacao, m.dataInicio) >= :inicio
           """)
    boolean existeConflito(@Param("veiculoId") Integer veiculoId,
                           @Param("inicio") LocalDate inicio,
                           @Param("fim") LocalDate fim,
                           @Param("ignorarId") Integer ignorarId);
}
