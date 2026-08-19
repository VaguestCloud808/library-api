package com.davi.biblioteca.controller;

import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.model.Multa;
import com.davi.biblioteca.repository.EmprestimoRepository;
import com.davi.biblioteca.scheduler.MultaScheduler;
import com.davi.biblioteca.service.MultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Tag(name = "Multa", description = "Endpoints do módulo de multas (JDBC). Valor: R$ 1,50/dia de atraso (arredondamento pra cima).")
public class MultaController {

    private final MultaService multaService;
    private final MultaScheduler multaScheduler;
    private final EmprestimoRepository emprestimoRepository;

    public MultaController(MultaService multaService,
                          MultaScheduler multaScheduler,
                          EmprestimoRepository emprestimoRepository) {
        this.multaService = multaService;
        this.multaScheduler = multaScheduler;
        this.emprestimoRepository = emprestimoRepository;
    }

    @GetMapping("/emprestimos/{emprestimoId}/multa")
    @Operation(summary = "Consulta a multa de um empréstimo",
               description = "Retorna a multa já gerada, ou calcula a prevista com base na data atual (se ainda não devolvido) ou data de devolução real.")
    public ResponseEntity<Multa> consultarMultaDoEmprestimo(@PathVariable Long emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.buscarPorId(emprestimoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Empréstimo com ID " + emprestimoId + " não encontrado"));
        LocalDateTime referencia = emprestimo.getDataDevolucaoReal() != null
                ? emprestimo.getDataDevolucaoReal()
                : LocalDateTime.now();
        Multa multa = multaService.buscarPorEmprestimoId(emprestimoId)
                .orElseGet(() -> multaService.calcularMultaPrevista(emprestimo, referencia));
        return ResponseEntity.ok(multa);
    }

    @GetMapping("/multas")
    @Operation(summary = "Lista multas (?atrasadas=true filtra não pagas)")
    public ResponseEntity<List<Multa>> listar(@RequestParam(value = "atrasadas", required = false) Boolean atrasadas) {
        boolean somenteAtrasadas = Boolean.TRUE.equals(atrasadas);
        return ResponseEntity.ok(multaService.listar(somenteAtrasadas));
    }

    @PostMapping("/multas/{id}/pagar")
    @Operation(summary = "Marca uma multa como paga")
    public ResponseEntity<Multa> pagar(@PathVariable Long id) {
        return ResponseEntity.ok(multaService.pagar(id));
    }

    @PostMapping("/multas/scheduler/disparar")
    @Operation(summary = "Dispara manualmente o scheduler de multas",
               description = "Útil pra testes. Em produção, o scheduler roda automaticamente 1x/dia.")
    public ResponseEntity<Integer> dispararScheduler() {
        int geradas = multaScheduler.executar(LocalDateTime.now());
        return ResponseEntity.ok(geradas);
    }
}