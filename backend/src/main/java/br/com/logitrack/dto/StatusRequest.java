package br.com.logitrack.dto;

import br.com.logitrack.domain.StatusManutencao;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(@NotNull StatusManutencao status) { }
