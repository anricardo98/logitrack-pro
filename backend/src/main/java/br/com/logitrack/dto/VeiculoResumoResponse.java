package br.com.logitrack.dto;

import br.com.logitrack.repository.VeiculoRepository.ResumoVeiculo;

public record VeiculoResumoResponse(Integer id, String placa, String modelo, String tipo, Integer ano,
                                    long viagens, long manutencoes, boolean podeExcluir) {

    public static VeiculoResumoResponse de(ResumoVeiculo r) {
        long viagens = r.getViagens(), manutencoes = r.getManutencoes();
        return new VeiculoResumoResponse(r.getId(), r.getPlaca(), r.getModelo(), r.getTipo(), r.getAno(),
                viagens, manutencoes, viagens == 0 && manutencoes == 0);
    }
}
