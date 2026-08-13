package com.davi.biblioteca.jpa.controller;

import com.davi.biblioteca.jpa.entity.LivroEntity;
import com.davi.biblioteca.jpa.service.JpaDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Controller alternativo que consome o {@link JpaDemoService} para demonstrar
 * que a API atende tanto clientes que preferem o estilo JDBC quanto JPA.
 *
 * Os endpoints ficam disponíveis em /api/jpa/livros para coexistir com /livros.
 */
@RestController
@RequestMapping("/api/jpa/livros")
@Tag(name = "Livro (JPA)", description = "Endpoints demonstrativos via Spring Data JPA")
public class JpaDemoController {

    private final JpaDemoService service;

    public JpaDemoController(JpaDemoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os livros via JPA")
    public ResponseEntity<List<LivroEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um livro por ID via JPA")
    public ResponseEntity<LivroEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria um livro via JPA")
    public ResponseEntity<LivroEntity> criar(@RequestBody LivroEntity livro) {
        LivroEntity salvo = service.criar(livro);
        return ResponseEntity.created(URI.create("/api/jpa/livros/" + salvo.getId())).body(salvo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um livro via JPA")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
