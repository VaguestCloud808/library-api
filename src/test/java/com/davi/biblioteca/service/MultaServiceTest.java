package com.davi.biblioteca.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.model.Multa;
import com.davi.biblioteca.repository.MultaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MultaServiceTest {

    private MultaRepository multaRepository;
    private MultaService multaService;

    @BeforeEach
    void setUp() {
        // cria um "dublê" do MultaRepository — finge ser o repository sem abrir conexão
        multaRepository = mock(MultaRepository.class);
        // instancia o service passando o dublê
        multaService = new MultaService(multaRepository);
    }

    // ========== calcularDiasAtraso ==========

    @Test
    @DisplayName("calcularDiasAtraso deve retornar 0 quando a devolução é antes do prazo")
    void calcularDiasAtraso_zeroQuandoDevolucaoAntesDoPrazo() {
        LocalDateTime prevista = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime referencia = LocalDateTime.of(2026, 1, 10, 10, 0); // 5 dias antes

        long dias = multaService.calcularDiasAtraso(prevista, referencia);

        assertEquals(0L, dias, "Devolução antes do prazo não gera atraso");
    }

    @Test
    @DisplayName("calcularDiasAtraso deve arredondar pra cima: meia hora de atraso conta como 1 dia")
    void calcularDiasAtraso_meiaHoraArredondaPraCima() {
        LocalDateTime prevista = LocalDateTime.of(2026, 1, 15, 10, 0);
        // 30 minutos de atraso
        LocalDateTime referencia = LocalDateTime.of(2026, 1, 15, 10, 30);

        long dias = multaService.calcularDiasAtraso(prevista, referencia);

        assertEquals(1L, dias, "Meia hora de atraso deve contar como 1 dia (arredondamento pra cima)");
    }

    @Test
    @DisplayName("calcularDiasAtraso deve contar dias inteiros corretamente")
    void calcularDiasAtraso_contaDiasInteiros() {
        LocalDateTime prevista = LocalDateTime.of(2026, 1, 15, 10, 0);
        LocalDateTime referencia = LocalDateTime.of(2026, 1, 18, 10, 0); // exatamente 3 dias

        long dias = multaService.calcularDiasAtraso(prevista, referencia);

        assertEquals(3L, dias);
    }

    @Test
    @DisplayName("calcularDiasAtraso deve retornar 0 quando datas são nulas (fail safe)")
    void calcularDiasAtraso_zeroQuandoDatasNulas() {
        assertEquals(0L, multaService.calcularDiasAtraso(null, LocalDateTime.now()));
        assertEquals(0L, multaService.calcularDiasAtraso(LocalDateTime.now(), null));
    }

    // ========== calcularValor ==========

    @Test
    @DisplayName("calcularValor deve retornar ZERO quando dias de atraso é 0 ou negativo")
    void calcularValor_zeroQuandoSemAtraso() {
        assertEquals(new BigDecimal("0.00"), multaService.calcularValor(0L));
        assertEquals(new BigDecimal("0.00"), multaService.calcularValor(-3L));
    }

    @Test
    @DisplayName("calcularValor deve multiplicar R$1,50 por dia (R$4,50 pra 3 dias)")
    void calcularValor_multiplicaPorDias() {
        BigDecimal valor = multaService.calcularValor(3L);

        assertEquals(new BigDecimal("4.50"), valor);
    }

    @Test
    @DisplayName("calcularValor deve sempre ter 2 casas decimais")
    void calcularValor_sempreDuasCasas() {
        BigDecimal valor = multaService.calcularValor(1L);

        assertEquals(2, valor.scale(), "Valor monetário deve ter sempre 2 casas decimais");
    }

    // ========== gerarMultaSeAtrasado ==========

    @Test
    @DisplayName("gerarMultaSeAtrasado deve ser idempotente: 2 chamadas = 1 multa salva")
    void gerarMultaSeAtrasado_idempotente() {
        Emprestimo emp = criarEmprestimo(1L);
        // prevista = 2026-01-15 10:00, referencia = 2026-01-18 10:00 = 3 dias atrasado
        LocalDateTime referencia = LocalDateTime.of(2026, 1, 18, 10, 0);

        // primeira chamada: multa não existe, então salva
        when(multaRepository.buscarPorEmprestimoId(1L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(multaSalva(1L, 3, "4.50")));
        when(multaRepository.salvar(any(Multa.class))).thenAnswer(inv -> inv.getArgument(0));

        Multa primeira = multaService.gerarMultaSeAtrasado(emp, referencia);
        Multa segunda = multaService.gerarMultaSeAtrasado(emp, referencia);

        assertNotNull(primeira);
        assertNotNull(segunda);
        assertEquals(3, primeira.getDiasAtraso());
        assertEquals(new BigDecimal("4.50"), primeira.getValor());
        // verifica que salvou APENAS UMA VEZ (idempotência)
        verify(multaRepository, times(1)).salvar(any(Multa.class));
    }

    @Test
    @DisplayName("gerarMultaSeAtrasado deve retornar null quando não há atraso (dias <= 0)")
    void gerarMultaSeAtrasado_retornaNullQuandoSemAtraso() {
        Emprestimo emp = criarEmprestimo(2L);
        LocalDateTime referencia = LocalDateTime.of(2026, 1, 10, 10, 0); // antes do prazo

        when(multaRepository.buscarPorEmprestimoId(2L)).thenReturn(Optional.empty());

        Multa resultado = multaService.gerarMultaSeAtrasado(emp, referencia);

        assertNull(resultado, "Sem atraso não deve gerar multa");
        verify(multaRepository, never()).salvar(any(Multa.class));
    }

    @Test
    @DisplayName("gerarMultaSeAtrasado deve lançar exceção quando empréstimo é nulo")
    void gerarMultaSeAtrasado_lancaExcecaoQuandoEmprestimoNulo() {
        assertThrows(DadosInvalidosException.class,
                () -> multaService.gerarMultaSeAtrasado(null, LocalDateTime.now()));
    }

    // ========== helpers ==========

    private Emprestimo criarEmprestimo(Long id) {
        Emprestimo emp = new Emprestimo();
        emp.setId(id);
        emp.setLivroId(10L);
        emp.setUsuarioId(20L);
        emp.setDataEmprestimo(LocalDateTime.of(2026, 1, 1, 10, 0));
        emp.setDataDevolucaoPrevista(LocalDateTime.of(2026, 1, 15, 10, 0));
        return emp;
    }

    private Multa multaSalva(Long emprestimoId, int dias, String valor) {
        Multa m = new Multa();
        m.setEmprestimoId(emprestimoId);
        m.setDiasAtraso(dias);
        m.setValor(new BigDecimal(valor));
        m.setPaga(false);
        return m;
    }
}
