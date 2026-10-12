package br.com.logitrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Os textos e diagramas Mermaid da documentação ficam em resources/openapi/*.md. */
@Configuration
public class OpenApiConfig {

    public static final String TAG_AUTH = "Autenticação";
    public static final String TAG_MANUTENCOES = "Manutenções";
    public static final String TAG_VEICULOS = "Veículos";
    public static final String TAG_DASHBOARD = "Dashboard";

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    OpenAPI logitrackOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("LogiTrack Pro API")
                        .version("1.0.0")
                        .description(texto("visao-geral"))
                        .contact(new Contact().name("André Ricardo Cândido Silva")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token devolvido por POST /api/auth/login")))
                .tags(List.of(
                        new Tag().name(TAG_AUTH).description(texto("autenticacao")),
                        new Tag().name(TAG_MANUTENCOES).description(texto("manutencoes")),
                        new Tag().name(TAG_VEICULOS).description(texto("veiculos")),
                        new Tag().name(TAG_DASHBOARD).description(texto("dashboard"))));
    }

    private static String texto(String nome) {
        try {
            return new ClassPathResource("openapi/" + nome + ".md").getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Texto da documentação não encontrado: " + nome, e);
        }
    }
}
