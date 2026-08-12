package com.davi.biblioteca.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.model.Multa;
import com.davi.biblioteca.repository.MultaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MultaService {

    static final BigDecimal VALOR_MULTA_POR_DIA = new BigDecimal("1.50");

    private final MultaRepository multaRepository;

    public MultaService(MultaRepository multaRepository) {
        this.multaRepository = multaRepository;
    }

    public long calcularDiasAtraso(LocalDateTime dataPrevista, LocalDateTime referencia) {
        if (dataPrevista == null || referencia == null) {
            return 0L;
        }
        long minutos = Duration.between(dataPrevista, referencia).toMinutes();
        // arredonda para cima: meia hora de atraso conta como 1 dia
        long dias = (minutos + (24L * 60) - 1) / (24L * 60);
        return Math.max(dias, 0L);
    }

    public BigDecimal calcularValor(long diasAtraso) {
        if (diasAtraso <= 0L) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return VALOR_MULTA_POR_DIA
                .multiply(BigDecimal.valueOf(diasAtraso))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Multa calcularMultaPrevista(Emprestimo emprestimo, LocalDateTime referencia) {
        if (emprestimo == null) {
            throw new DadosInvalidosException("Empréstimo não pode ser nulo");
        }
        long dias = calcularDiasAtraso(emprestimo.getDataDevolucaoPrevista(), referencia);
        Multa m = new Multa();
        m.setEmprestimoId(emprestimo.getId());
        m.setDiasAtraso((int) dias);
        m.setValor(calcularValor(dias));
        m.setPaga(false);
        return m;
    }

    public Optional<Multa> buscarPorEmprestimoId(Long emprestimoId) {
        if (emprestimoId == null) {
            throw new DadosInvalidosException("ID do empréstimo não pode ser nulo");
        }
        return multaRepository.buscarPorEmprestimoId(emprestimoId);
    }

    public Multa gerarMultaSeAtrasado(Emprestimo emprestimo, LocalDateTime referencia) {
        if (emprestimo == null || emprestimo.getId() == null) {
            throw new DadosInvalidosException("Empréstimo inválido para geração de multa");
        }
        Optional<Multa> existente = multaRepository.buscarPorEmprestimoId(emprestimo.getId());
        if (existente.isPresent()) {
            return existente.get();
        }
        long dias = calcularDiasAtraso(emprestimo.getDataDevolucaoPrevista(), referencia);
        if (dias <= 0L) {
            return null;
        }
        Multa nova = new Multa();
        nova.setEmprestimoId(emprestimo.getId());
        nova.setDiasAtraso((int) dias);
        nova.setValor(calcularValor(dias));
        nova.setPaga(false);
        return multaRepository.salvar(nova);
    }

    public boolean gerarMultaNovaSeAtrasado(Emprestimo emprestimo, LocalDateTime referencia) {
        if (multaRepository.existePorEmprestimoId(emprestimo.getId())) {
            return false;
        }
        Multa antes = multaRepository.buscarPorEmprestimoId(emprestimo.getId()).orElse(null);
        Multa depois = gerarMultaSeAtrasado(emprestimo, referencia);
        return antes == null && depois != null;
    }

    public Multa pagar(Long multaId) {
        if (multaId == null) {
            throw new DadosInvalidosException("ID da multa não pode ser nulo");
        }
        Multa multa = multaRepository.buscarPorId(multaId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Multa com ID " + multaId + " não encontrada"));
        if (Boolean.TRUE.equals(multa.getPaga())) {
            throw new DadosInvalidosException("Multa " + multaId + " já foi paga");
        }
        return multaRepository.marcarComoPaga(multaId);
    }

    public List<Multa> listar(boolean somenteAtrasadas) {
        return somenteAtrasadas ? multaRepository.listarEmAberto() : multaRepository.listar();
    }
}