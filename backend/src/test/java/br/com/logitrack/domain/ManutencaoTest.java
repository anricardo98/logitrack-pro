package br.com.logitrack.domain;

import br.com.logitrack.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Testes das regras de negócio que vivem na própria entidade (sem Spring, sem banco). */
class ManutencaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 10, 7);

    @Test
    void statusPadraoEhPendente() {
        Manutencao m = new Manutencao(null, HOJE, HOJE.plusDays(1), " Troca de Óleo ", BigDecimal.TEN, null);
        assertThat(m.getStatus()).isEqualTo(StatusManutencao.PENDENTE);
        assertThat(m.getTipoServico()).isEqualTo("Troca de Óleo");
    }

    @Test
    void naoAceitaFinalizacaoAntesDoInicio() {
        assertThatThrownBy(() -> new Manutencao(null, HOJE, HOJE.minusDays(1), "Freios", null, null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void concluidaExigeDataDeFinalizacao() {
        assertThatThrownBy(() -> new Manutencao(null, HOJE, null, "Pneus", null, StatusManutencao.CONCLUIDA))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void concluirSemDataPreencheFinalizacao() {
        Manutencao m = new Manutencao(null, HOJE.minusDays(2), null, "Motor", null, StatusManutencao.EM_REALIZACAO);
        m.alterarStatus(StatusManutencao.CONCLUIDA);
        assertThat(m.getStatus()).isEqualTo(StatusManutencao.CONCLUIDA);
        assertThat(m.getDataFinalizacao()).isNotNull();
    }
}
