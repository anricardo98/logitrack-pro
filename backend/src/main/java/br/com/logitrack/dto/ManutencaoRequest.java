package br.com.logitrack.dto;

import br.com.logitrack.domain.StatusManutencao;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Informe veiculoId OU novoVeiculo (nunca os dois)")
public record ManutencaoRequest(
        @Schema(description = "Veículo já cadastrado", example = "1")
        Integer veiculoId,
        @Schema(description = "Veículo a cadastrar junto com a manutenção")
        @Valid VeiculoRequest novoVeiculo,
        @Schema(example = "2026-10-20")
        @NotNull(message = "Informe a data de início") LocalDate dataInicio,
        @Schema(description = "Finalização prevista", example = "2026-10-21")
        LocalDate dataFinalizacao,
        @Schema(example = "Troca de Óleo")
        @NotBlank(message = "Informe o tipo de serviço") @Size(max = 100) String tipoServico,
        @Schema(example = "350.00")
        @PositiveOrZero(message = "O custo não pode ser negativo")
        @Digits(integer = 8, fraction = 2) BigDecimal custoEstimado,
        @Schema(description = "Padrão: PENDENTE")
        StatusManutencao status
) {
    // Bean Validation expõe este método como a propriedade "veiculoInformado" (o front mapeia o erro por esse nome).
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "Selecione um veículo cadastrado ou informe os dados de um novo veículo")
    public boolean isVeiculoInformado() {
        return (veiculoId == null) != (novoVeiculo == null);
    }
}
