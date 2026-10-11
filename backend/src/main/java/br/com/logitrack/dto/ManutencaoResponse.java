package br.com.logitrack.dto;

import br.com.logitrack.domain.Manutencao;
import br.com.logitrack.domain.StatusManutencao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ManutencaoResponse(
        Integer id,
        VeiculoResponse veiculo,
        LocalDate dataInicio,
        LocalDate dataFinalizacao,
        String tipoServico,
        BigDecimal custoEstimado,
        StatusManutencao status
) {
    public static ManutencaoResponse de(Manutencao m) {
        return new ManutencaoResponse(m.getId(), VeiculoResponse.de(m.getVeiculo()), m.getDataInicio(),
                m.getDataFinalizacao(), m.getTipoServico(), m.getCustoEstimado(), m.getStatus());
    }
}
