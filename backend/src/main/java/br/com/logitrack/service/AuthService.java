package br.com.logitrack.service;

import br.com.logitrack.domain.Usuario;
import br.com.logitrack.dto.LoginRequest;
import br.com.logitrack.dto.LoginResponse;
import br.com.logitrack.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwtEncoder;
    private final Duration validade;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtEncoder jwtEncoder,
                       @Value("${app.jwt.validade-horas:8}") long horas) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwtEncoder = jwtEncoder;
        this.validade = Duration.ofHours(horas);
    }

    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(req.email())
                .filter(u -> encoder.matches(req.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new BadCredentialsException("credenciais"));

        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("logitrack")
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim("nome", usuario.getNome())
                .claim("perfil", usuario.getPerfil())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new LoginResponse(token, usuario.getNome(), usuario.getEmail(), validade.toSeconds());
    }
}
