package br.com.logitrack.controller;

import br.com.logitrack.config.OpenApiConfig;
import br.com.logitrack.domain.TipoVeiculo;
import br.com.logitrack.dto.VeiculoRequest;
import br.com.logitrack.dto.VeiculoResponse;
import br.com.logitrack.dto.VeiculoResumoResponse;
import br.com.logitrack.service.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/veiculos")
@Tag(name = OpenApiConfig.TAG_VEICULOS)
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista veículos", description = "Lista simples ordenada pela placa, usada nos campos de seleção.")
    @ApiResponse(responseCode = "200", description = "Veículos cadastrados")
    public List<VeiculoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/resumo")
    @Operation(summary = "Lista veículos com uso",
            description = "Traz, para cada veículo, quantas viagens e manutenções ele tem e se pode ser excluído.")
    @ApiResponse(responseCode = "200", description = "Veículos com contagem de uso")
    public List<VeiculoResumoResponse> resumo(
            @Parameter(description = "Filtra pela categoria") @RequestParam(required = false) TipoVeiculo tipo,
            @Parameter(description = "Trecho da placa ou do modelo", example = "scania") @RequestParam(required = false) String busca) {
        return service.resumo(tipo, busca);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um veículo")
    @ApiResponse(responseCode = "200", description = "Veículo encontrado")
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public VeiculoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra um veículo")
    @ApiResponse(responseCode = "201", description = "Veículo criado; o cabeçalho Location aponta para ele")
    @ApiResponse(responseCode = "400", description = "Campos inválidos (placa fora do padrão, ano fora de 1950–2100...)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Placa já cadastrada",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody VeiculoRequest req) {
        VeiculoResponse criado = VeiculoResponse.de(service.cadastrar(req));
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um veículo")
    @ApiResponse(responseCode = "200", description = "Veículo atualizado")
    @ApiResponse(responseCode = "400", description = "Campos inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Placa já usada por outro veículo",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public VeiculoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody VeiculoRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui um veículo", description = "Só é permitido para veículos sem viagens nem manutenções.")
    @ApiResponse(responseCode = "204", description = "Excluído")
    @ApiResponse(responseCode = "404", description = "Veículo não encontrado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Veículo com histórico não pode ser excluído",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
