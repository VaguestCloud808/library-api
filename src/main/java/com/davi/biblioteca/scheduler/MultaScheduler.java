package com.davi.biblioteca.scheduler;

import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.repository.EmprestimoRepository;
import com.davi.biblioteca.service.MultaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MultaScheduler {

    private static final Logger log = LoggerFactory.getLogger(MultaScheduler.class);

    private final EmprestimoRepository emprestimoRepository;
    private final MultaService multaService;

    public MultaScheduler(EmprestimoRepository emprestimoRepository, MultaService multaService) {
        this.emprestimoRepository = emprestimoRepository;
        this.multaService = multaService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void gerarMultasAtrasadas() {
        executar(LocalDateTime.now());
    }

    public int executar(LocalDateTime referencia) {
        List<Emprestimo> emprestimos = emprestimoRepository.listar();
        int geradas = 0;
        for (Emprestimo emp : emprestimos) {
            if (emp.getDataDevolucaoReal() != null) {
                continue;
            }
            if (!emp.getDataDevolucaoPrevista().isBefore(referencia)) {
                continue;
            }
            try {
                if (multaService.gerarMultaSeAtrasado(emp, referencia) != null) {
                    geradas++;
                }
            } catch (RuntimeException ex) {
                log.warn("Falha ao gerar multa para emprestimo {}: {}", emp.getId(), ex.getMessage());
            }
        }
        log.info("Scheduler de multas: {} gerada(s) de {} emprestimo(s) analisado(s)", geradas, emprestimos.size());
        return geradas;
    }
}