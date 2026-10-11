package br.com.logitrack.domain;

import br.com.logitrack.exception.RegraNegocioException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Agendamento de manutenção de um veículo.
 * As regras que dependem apenas do próprio objeto ficam aqui (modelo rico);
 * regras que dependem de outros registros ficam no service.
 */
@Entity
@Table(name = "manutencoes")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_finalizacao")
    private LocalDate dataFinalizacao;

    @Column(name = "tipo_servico", nullable = false, length = 100)
    private String tipoServico;

    @Column(name = "custo_estimado", precision = 10, scale = 2)
    private BigDecimal custoEstimado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusManutencao status = StatusManutencao.PENDENTE;

    protected Manutencao() { }

    public Manutencao(Veiculo veiculo, LocalDate dataInicio, LocalDate dataFinalizacao,
                      String tipoServico, BigDecimal custoEstimado, StatusManutencao status) {
        atualizar(veiculo, dataInicio, dataFinalizacao, tipoServico, custoEstimado, status);
    }

    public void atualizar(Veiculo veiculo, LocalDate dataInicio, LocalDate dataFinalizacao,
                          String tipoServico, BigDecimal custoEstimado, StatusManutencao status) {
        if (dataFinalizacao != null && dataFinalizacao.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data de finalização não pode ser anterior à data de início.");
        }
        if (status == StatusManutencao.CONCLUIDA && dataFinalizacao == null) {
            throw new RegraNegocioException("Informe a data de finalização para concluir a manutenção.");
        }
        this.veiculo = veiculo;
        this.dataInicio = dataInicio;
        this.dataFinalizacao = dataFinalizacao;
        this.tipoServico = tipoServico.trim();
        this.custoEstimado = custoEstimado;
        this.status = status != null ? status : StatusManutencao.PENDENTE;
    }

    public void alterarStatus(StatusManutencao novoStatus) {
        atualizar(veiculo, dataInicio,
                novoStatus == StatusManutencao.CONCLUIDA && dataFinalizacao == null ? LocalDate.now() : dataFinalizacao,
                tipoServico, custoEstimado, novoStatus);
    }

    public Integer getId() { return id; }
    public Veiculo getVeiculo() { return veiculo; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFinalizacao() { return dataFinalizacao; }
    public String getTipoServico() { return tipoServico; }
    public BigDecimal getCustoEstimado() { return custoEstimado; }
    public StatusManutencao getStatus() { return status; }
}
