package br.com.logitrack.service;

import br.com.logitrack.domain.TipoVeiculo;
import br.com.logitrack.domain.Veiculo;
import br.com.logitrack.dto.VeiculoRequest;
import br.com.logitrack.dto.VeiculoResponse;
import br.com.logitrack.dto.VeiculoResumoResponse;
import br.com.logitrack.exception.RecursoNaoEncontradoException;
import br.com.logitrack.exception.RegraNegocioException;
import br.com.logitrack.repository.VeiculoRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculos;

    public VeiculoService(VeiculoRepository veiculos) {
        this.veiculos = veiculos;
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listar() {
        return veiculos.findAll(Sort.by("placa")).stream().map(VeiculoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<VeiculoResumoResponse> resumo(TipoVeiculo tipo, String busca) {
        return veiculos.resumo(tipo, busca).stream().map(VeiculoResumoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscar(Integer id) {
        return VeiculoResponse.de(obter(id));
    }

    @Transactional
    public Veiculo cadastrar(VeiculoRequest req) {
        String placa = Veiculo.normalizarPlaca(req.placa());
        if (veiculos.existsByPlaca(placa)) {
            throw placaDuplicada(placa);
        }
        return veiculos.save(new Veiculo(placa, req.modelo(), req.tipo(), req.ano()));
    }

    @Transactional
    public VeiculoResponse atualizar(Integer id, VeiculoRequest req) {
        Veiculo veiculo = obter(id);
        String placa = Veiculo.normalizarPlaca(req.placa());
        if (veiculos.existsByPlacaAndIdNot(placa, id)) {
            throw placaDuplicada(placa);
        }
        veiculo.atualizar(placa, req.modelo(), req.tipo(), req.ano());
        return VeiculoResponse.de(veiculo);
    }

    /** Veículo com histórico não é excluído: levaria junto a quilometragem e os custos do dashboard. */
    @Transactional
    public void excluir(Integer id) {
        Veiculo veiculo = obter(id);
        if (veiculos.contarVinculos(id) > 0) {
            throw new RegraNegocioException("O veículo " + veiculo.getPlaca()
                    + " possui viagens ou manutenções registradas e não pode ser excluído.");
        }
        veiculos.delete(veiculo);
    }

    @Transactional(readOnly = true)
    public Veiculo obter(Integer id) {
        return veiculos.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Veículo", id));
    }

    private RegraNegocioException placaDuplicada(String placa) {
        return new RegraNegocioException("Já existe um veículo com a placa " + placa + ".");
    }
}
