package br.com.logitrack.repository;

import br.com.logitrack.domain.TipoVeiculo;
import br.com.logitrack.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Integer> {

    boolean existsByPlaca(String placa);

    /** Placa repetida na edição: outra linha (id diferente) já usa essa placa. */
    boolean existsByPlacaAndIdNot(String placa, Integer id);

    /**
     * Listagem da tela de veículos com quantas viagens e manutenções cada um tem.
     * Subconsultas correlacionadas usam os índices de veiculo_id criados na V2.
     */
    @Query(value = """
            SELECT v.id, v.placa, v.modelo, v.tipo, v.ano,
                   (SELECT COUNT(*) FROM viagens vi     WHERE vi.veiculo_id = v.id) AS viagens,
                   (SELECT COUNT(*) FROM manutencoes m  WHERE m.veiculo_id  = v.id) AS manutencoes
            FROM veiculos v
            WHERE (CAST(:tipo AS VARCHAR) IS NULL OR v.tipo = CAST(:tipo AS VARCHAR))
              AND (CAST(:busca AS VARCHAR) IS NULL
                   OR v.placa  ILIKE '%' || CAST(:busca AS VARCHAR) || '%'
                   OR v.modelo ILIKE '%' || CAST(:busca AS VARCHAR) || '%')
            ORDER BY v.placa
            """, nativeQuery = true)
    List<ResumoVeiculo> resumoNativo(@Param("tipo") String tipo, @Param("busca") String busca);

    @Query(value = """
            SELECT (SELECT COUNT(*) FROM viagens     WHERE veiculo_id = :id)
                 + (SELECT COUNT(*) FROM manutencoes WHERE veiculo_id = :id)
            """, nativeQuery = true)
    long contarVinculos(@Param("id") Integer id);

    /** Projeção (interface) preenchida pelo Spring Data a partir das colunas da consulta nativa. */
    interface ResumoVeiculo {
        Integer getId();
        String getPlaca();
        String getModelo();
        String getTipo();
        Integer getAno();
        Long getViagens();
        Long getManutencoes();
    }

    default List<ResumoVeiculo> resumo(TipoVeiculo tipo, String busca) {
        return resumoNativo(tipo != null ? tipo.name() : null, busca == null || busca.isBlank() ? null : busca.trim());
    }
}
