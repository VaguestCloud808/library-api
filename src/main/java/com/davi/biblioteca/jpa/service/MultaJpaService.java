package com.davi.biblioteca.jpa.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.jpa.entity.MultaEntity;
import com.davi.biblioteca.jpa.repository.MultaJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Versão JPA (demonstrativa) do {@link com.davi.biblioteca.service.MultaService}.
 *
 * Cálculo de multa é puro Java (sem SQL) — fica na camada de serviço. O
 * repository JPA só persiste e busca.
 */
@Service
public class MultaJpaService {

    private static final BigDecimal VALOR_MULTA_POR_DIA = new BigDecimal("1.50");

    private final MultaJpaRepository repository;

    public MultaJpaService(MultaJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<MultaEntity> listarTodas() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<MultaEntity> listarEmAberto() {
        return repository.findByPagaFalseOrderByDataGeracaoDesc();
    }

    @Transactional(readOnly = true)
    public MultaEntity buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Multa com ID " + id + " não encontrada (via JPA)"));
    }

    @Transactional
    public MultaEntity pagar(Long id) {
        MultaEntity multa = buscarPorId(id);
        if (Boolean.TRUE.equals(multa.getPaga())) {
            throw new DadosInvalidosException("Multa " + id + " já foi paga");
        }
        multa.setPaga(true);
        multa.setDataPagamento(LocalDateTime.now());
        return repository.save(multa);
    }

    @Transactional
    public MultaEntity gerar(Long emprestimoId, LocalDateTime dataPrevista, LocalDateTime referencia) {
        if (repository.existsByEmprestimoId(emprestimoId)) {
            return repository.findByEmprestimoId(emprestimoId).orElseThrow();
        }
        long dias = calcularDiasAtraso(dataPrevista, referencia);
        if (dias <= 0) {
            return null;
        }
        MultaEntity multa = new MultaEntity();
        multa.setEmprestimoId(emprestimoId);
        multa.setDiasAtraso((int) dias);
        multa.setValor(calcularValor(dias));
        multa.setPaga(false);
        return repository.save(multa);
    }

    static long calcularDiasAtraso(LocalDateTime dataPrevista, LocalDateTime referencia) {
        if (dataPrevista == null || referencia == null) {
            return 0L;
        }
        long minutos = Duration.between(dataPrevista, referencia).toMinutes();
        long dias = (minutos + (24L * 60) - 1) / (24L * 60);
        return Math.max(dias, 0L);
    }

    static BigDecimal calcularValor(long diasAtraso) {
        if (diasAtraso <= 0L) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return VALOR_MULTA_POR_DIA
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
