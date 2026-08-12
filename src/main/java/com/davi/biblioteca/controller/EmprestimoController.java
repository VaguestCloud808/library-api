package com.davi.biblioteca.controller;

import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.service.EmprestimoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService service;

    public EmprestimoController(EmprestimoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Emprestimo> realizar(@RequestBody Map<String, Long> body) {
        Long livroId = body.get("livroId");
        Long usuarioId = body.get("usuarioId");
        Emprestimo emp = service.realizar(livroId, usuarioId);
        return ResponseEntity.created(URI.create("/emprestimos/" + emp.getId())).body(emp);
    }

    @GetMapping
    public ResponseEntity<List<Emprestimo>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscarPorId(@PathVariable Long id) {
        Optional<Emprestimo> emp = service.buscarPorId(id);
        if (emp.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Empréstimo com ID " + id + " não encontrado");
        }
        return ResponseEntity.ok(emp.get());
    }

    @PostMapping("/{id}/devolucao")
    public ResponseEntity<Emprestimo> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.devolver(id));
    }
}
