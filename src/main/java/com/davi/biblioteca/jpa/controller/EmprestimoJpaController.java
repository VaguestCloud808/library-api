package com.davi.biblioteca.jpa.controller;

import com.davi.biblioteca.jpa.entity.EmprestimoEntity;
import com.davi.biblioteca.jpa.service.EmprestimoJpaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Controller demonstrativo JPA para empréstimos. Convivência lado a lado com o
 * controller JDBC em /emprestimos.
 */
@RestController
@RequestMapping("/api/jpa/emprestimos")
@Tag(name = "Empréstimo (JPA)", description = "Endpoints demonstrativos via Spring Data JPA")
public class EmprestimoJpaController {

    private final EmprestimoJpaService service;

    public EmprestimoJpaController(EmprestimoJpaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os empréstimos via JPA")
    public ResponseEntity<List<EmprestimoEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um empréstimo por ID via JPA")
    public ResponseEntity<EmprestimoEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria um empréstimo via JPA (body: {livroId, usuarioId})")
    public ResponseEntity<EmprestimoEntity> criar(@RequestBody Map<String, Long> body) {
        Long livroId = body.get("livroId");
        Long usuarioId = body.get("usuarioId");
        EmprestimoEntity salvo = service.realizar(livroId, usuarioId);
        return ResponseEntity.created(URI.create("/api/jpa/emprestimos/" + salvo.getId())).body(salvo);
    }

    @PostMapping("/{id}/devolucao")
    @Operation(summary = "Registra devolução via JPA")
    public ResponseEntity<EmprestimoEntity> devolver(@PathVariable Long id) {
        return ResponseEntity.ok(service.devolver(id));
    }
}
