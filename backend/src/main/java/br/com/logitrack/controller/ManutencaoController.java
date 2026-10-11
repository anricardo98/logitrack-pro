package br.com.logitrack.controller;

import br.com.logitrack.config.OpenApiConfig;
import br.com.logitrack.domain.StatusManutencao;
import br.com.logitrack.dto.ManutencaoRequest;
import br.com.logitrack.dto.ManutencaoResponse;
import br.com.logitrack.dto.StatusRequest;
import br.com.logitrack.service.ManutencaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/manutencoes")
@Tag(name = OpenApiConfig.TAG_MANUTENCOES)
public class ManutencaoController {

    private final ManutencaoService service;

    private static final String EXEMPLO_EXISTENTE = """
            {"veiculoId": 1, "dataInicio": "2026-10-20", "dataFinalizacao": "2026-10-21",
             "tipoServico": "Troca de Óleo", "custoEstimado": 350.00, "status": "PENDENTE"}""";

    private static final String EXEMPLO_NOVO = """
            {"novoVeiculo": {"placa": "BRA2E19", "modelo": "Iveco Daily", "tipo": "LEVE", "ano": 2024},
             "dataInicio": "2026-10-20", "dataFinalizacao": "2026-10-21",
             "tipoServico": "Revisão de Motor", "custoEstimado": 1200.00}""";

    public ManutencaoController(ManutencaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista manutenções",
            description = "Paginada e com filtros opcionais por status e veículo. Padrão: 10 por página, ordenadas pela data de início.")
    @ApiResponse(responseCode = "200", description = "Página de manutenções")
    public Page<ManutencaoResponse> listar(
            @Parameter(description = "Filtra pelo status") @RequestParam(required = false) StatusManutencao status,
            @Parameter(description = "Filtra pelo veículo", example = "1") @RequestParam(required = false) Integer veiculoId,
            @ParameterObject @PageableDefault(size = 10, sort = "dataInicio", direction = Sort.Direction.ASC) Pageable pageable) {
        return service.listar(status, veiculoId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma manutenção")
    @ApiResponse(responseCode = "200", description = "Manutenção encontrada")
    @ApiResponse(responseCode = "404", description = "Manutenção não encontrada",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ManutencaoResponse buscar(@PathVariable Integer id) {
        return service.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Agenda uma manutenção",
            description = "Informe `veiculoId` para um veículo já cadastrado **ou** `novoVeiculo` para cadastrar o veículo junto. "
                    + "Veja o diagrama de sequência na descrição da seção.")
    @ApiResponse(responseCode = "201", description = "Manutenção criada; o cabeçalho Location aponta para ela")
    @ApiResponse(responseCode = "400", description = "Campos inválidos (o mapa `campos` traz a mensagem de cada um)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "veiculoId não encontrado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio: datas inválidas, conflito de agenda ou placa repetida",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<ManutencaoResponse> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = {
                    @ExampleObject(name = "Veículo já cadastrado", value = EXEMPLO_EXISTENTE),
                    @ExampleObject(name = "Com veículo novo", value = EXEMPLO_NOVO)}))
            @Valid @RequestBody ManutencaoRequest req) {
        ManutencaoResponse criada = service.criar(req);
        var uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma manutenção", description = "Substitui todos os campos. Aceita `veiculoId` ou `novoVeiculo`, como no cadastro.")
    @ApiResponse(responseCode = "200", description = "Manutenção atualizada")
    @ApiResponse(responseCode = "400", description = "Campos inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Manutenção ou veículo não encontrado",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Regra de negócio violada",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ManutencaoResponse atualizar(@PathVariable Integer id, @Valid @RequestBody ManutencaoRequest req) {
        return service.atualizar(id, req);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Altera só o status",
            description = "Ao concluir sem data de finalização, a data de hoje é preenchida automaticamente.")
    @ApiResponse(responseCode = "200", description = "Status alterado")
    @ApiResponse(responseCode = "404", description = "Manutenção não encontrada",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ManutencaoResponse alterarStatus(@PathVariable Integer id, @Valid @RequestBody StatusRequest req) {
        return service.alterarStatus(id, req.status());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma manutenção")
    @ApiResponse(responseCode = "204", description = "Excluída")
    @ApiResponse(responseCode = "404", description = "Manutenção não encontrada",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
