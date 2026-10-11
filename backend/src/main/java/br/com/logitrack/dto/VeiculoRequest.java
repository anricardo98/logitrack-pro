package br.com.logitrack.dto;

import br.com.logitrack.domain.TipoVeiculo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * Dados para cadastrar um veículo. A placa aceita o padrão antigo (ABC-1234)
 * e o Mercosul (ABC1D23), com ou sem hífen.
 */
public record VeiculoRequest(
        @Schema(description = "Padrão antigo ou Mercosul, com ou sem hífen", example = "BRA2E19")
        @NotBlank(message = "Informe a placa")
        @Pattern(regexp = "(?i)^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$",
                 message = "Placa inválida (ex.: ABC-1234 ou ABC1D23)")
        String placa,

        @Schema(example = "Iveco Daily")
        @NotBlank(message = "Informe o modelo")
        @Size(max = 50, message = "O modelo deve ter no máximo 50 caracteres")
        String modelo,

        @Schema(example = "LEVE")
        @NotNull(message = "Selecione o tipo")
        TipoVeiculo tipo,

        @Schema(example = "2024")
        @Min(value = 1950, message = "Ano inválido")
        @Max(value = 2100, message = "Ano inválido")
        Integer ano
) { }
