package br.com.logitrack.config;

import br.com.logitrack.domain.Usuario;
import br.com.logitrack.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria o usuário administrador na primeira execução.
 * O hash é gerado pelo PasswordEncoder da aplicação (nunca fica fixo no SQL),
 * e e-mail/senha podem ser sobrescritos por variáveis de ambiente.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String email;
    private final String senha;

    public DataSeeder(UsuarioRepository usuarios, PasswordEncoder encoder,
                      @Value("${app.admin.email}") String email,
                      @Value("${app.admin.senha}") String senha) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        usuarios.save(new Usuario("Administrador", email, encoder.encode(senha), "ADMIN"));
        log.info("Usuário administrador criado: {}", email);
    }
}
