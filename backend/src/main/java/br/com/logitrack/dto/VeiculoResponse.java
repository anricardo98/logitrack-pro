package br.com.logitrack.dto;

import br.com.logitrack.domain.TipoVeiculo;
import br.com.logitrack.domain.Veiculo;

public record VeiculoResponse(Integer id, String placa, String modelo, TipoVeiculo tipo, Integer ano) {
    public static VeiculoResponse de(Veiculo v) {
        return new VeiculoResponse(v.getId(), v.getPlaca(), v.getModelo(), v.getTipo(), v.getAno());
    }
}
