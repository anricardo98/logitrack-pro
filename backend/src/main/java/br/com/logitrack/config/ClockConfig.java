package br.com.logitrack.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Clock injetável: deixa "hoje" testável e fixa o fuso de operação da empresa. */
@Configuration
public class ClockConfig {
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("America/Sao_Paulo"));
    }
}
