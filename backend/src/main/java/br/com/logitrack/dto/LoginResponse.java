package br.com.logitrack.dto;

public record LoginResponse(String token, String nome, String email, long expiraEmSegundos) { }
