package com.davi.biblioteca.jpa.controller;

import com.davi.biblioteca.jpa.entity.UsuarioEntity;
import com.davi.biblioteca.jpa.service.UsuarioJpaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Controller demonstrativo JPA para usuários. Convivência lado a lado com o
 * controller JDBC em /usuarios. Prefixo /api/jpa para deixar claro o "stack".
 */
@RestController
@RequestMapping("/api/jpa/usuarios")
@Tag(name = "Usuário (JPA)", description = "Endpoints demonstrativos via Spring Data JPA")
public class UsuarioJpaController {

    private final UsuarioJpaService service;

    public UsuarioJpaController(UsuarioJpaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários via JPA")
    public ResponseEntity<List<UsuarioEntity>> listar() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário por ID via JPA")
    public ResponseEntity<UsuarioEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Cria um usuário via JPA")
    public ResponseEntity<UsuarioEntity> criar(@RequestBody UsuarioEntity usuario) {
        UsuarioEntity salvo = service.criar(usuario);
        return ResponseEntity.created(URI.create("/api/jpa/usuarios/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuário via JPA")
    public ResponseEntity<UsuarioEntity> atualizar(@PathVariable Long id,
                                                   @RequestBody UsuarioEntity usuario) {
        return ResponseEntity.ok(service.atualizar(id, usuario));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um usuário via JPA")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
