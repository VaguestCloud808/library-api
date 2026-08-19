package com.davi.biblioteca.controller;

import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.service.EmprestimoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/emprestimos")
@Tag(name = "Empréstimo", description = "Endpoints do módulo de empréstimos (JDBC). Prazo de devolução: 14 dias.")
public class EmprestimoController {

    private final EmprestimoService service;

    public EmprestimoController(EmprestimoService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Realiza um empréstimo",
               description = "Body: { \"livroId\": 1, \"usuarioId\": 2 }. Decrementa a quantidade disponível do livro atomicamente. Regras: livro com exemplares, usuário ativo.")
    public ResponseEntity<Emprestimo> realizar(@RequestBody Map<String, Long> body) {
        Long livroId = body.get("livroId");
        Long usuarioId = body.get("usuarioId");
        Emprestimo emp = service.realizar(livroId, usuarioId);
        return ResponseEntity.created(URI.create("/emprestimos/" + emp.getId())).body(emp);
    }

    @GetMapping
    @Operation(summary = "Lista todos os empréstimos")
    public ResponseEntity<List<Emprestimo>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um empréstimo por ID")
    public ResponseEntity<Emprestimo> buscarPorId(@PathVariable Long id) {
        Optional<Emprestimo> emp = service.buscarPorId(id);
        if (emp.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Empréstimo com ID " + id + " não encontrado");
        }
        return ResponseEntity.ok(emp.get());
    }

    @PostMapping("/{id}/devolucao")
    @Operation(summary = "Registra a devolução de um empréstimo",
               description = "Incrementa a quantidade disponível do livro e, se atrasado, gera multa automaticamente.")
    public ResponseEntity<Emprestimo> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.devolver(id));
    }
}
