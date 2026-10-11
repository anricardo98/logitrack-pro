package br.com.logitrack.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "admin@logitrack.com") @NotBlank @Email String email,
        @Schema(example = "admin123") @NotBlank String senha
) { }
