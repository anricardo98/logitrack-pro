package br.com.logitrack.controller;

import br.com.logitrack.config.OpenApiConfig;
import br.com.logitrack.dto.DashboardDtos.*;
import br.com.logitrack.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = OpenApiConfig.TAG_DASHBOARD)
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "As 5 métricas de uma vez", description = "Usado pela tela do dashboard para abrir com uma única requisição.")
    public Resumo resumo(@Parameter(description = "Veículo para o total de KM; vazio = frota toda", example = "1")
                         @RequestParam(required = false) Integer veiculoId) {
        return service.resumo(veiculoId);
    }

    @GetMapping("/km-total")
    @Operation(summary = "Total de KM percorrido", description = "De um veículo específico ou de toda a frota.")
    public TotalKm totalKm(@Parameter(description = "Vazio = frota toda", example = "1")
                           @RequestParam(required = false) Integer veiculoId) {
        return service.totalKm(veiculoId);
    }

    @GetMapping("/volume-categoria")
    @Operation(summary = "Volume por categoria", description = "Quantidade de viagens por tipo de veículo (Leve x Pesado).")
    public List<VolumeCategoria> volume(@Parameter(description = "Filtra uma categoria",
                                                   schema = @Schema(allowableValues = {"LEVE", "PESADO"}))
                                        @RequestParam(required = false) String tipo) {
        return service.volumePorCategoria(tipo);
    }

    @GetMapping("/cronograma")
    @Operation(summary = "Cronograma de manutenção", description = "As próximas 5 manutenções não concluídas, ordenadas por data.")
    public List<ProximaManutencao> cronograma() {
        return service.cronograma();
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking de utilização", description = "Veículos ordenados pela soma de KM; o primeiro é o mais utilizado.")
    public List<RankingVeiculo> ranking() {
        return service.ranking();
    }

    @GetMapping("/projecao-financeira")
    @Operation(summary = "Projeção financeira", description = "Soma do custo estimado das manutenções do mês atual.")
    public ProjecaoFinanceira projecao() {
        return service.projecaoMesAtual();
    }
}
