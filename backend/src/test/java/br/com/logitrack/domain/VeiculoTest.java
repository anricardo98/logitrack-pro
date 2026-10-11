package br.com.logitrack.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VeiculoTest {

    @Test
    void placaAntigaGanhaHifenEMaiusculas() {
        assertThat(Veiculo.normalizarPlaca(" abc1234 ")).isEqualTo("ABC-1234");
        assertThat(Veiculo.normalizarPlaca("ABC-1234")).isEqualTo("ABC-1234");
    }

    @Test
    void placaMercosulFicaSemHifen() {
        assertThat(Veiculo.normalizarPlaca("bra2e19")).isEqualTo("BRA2E19");
        assertThat(Veiculo.normalizarPlaca("BRA-2E19")).isEqualTo("BRA2E19");
    }
}
