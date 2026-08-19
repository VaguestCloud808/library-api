package com.davi.biblioteca.controller;

import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.model.Usuario;
import com.davi.biblioteca.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuário", description = "Endpoints do módulo de usuários (JDBC)")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria um usuário", description = "Cadastra um novo usuário. Email e CPF devem ser únicos.")
    public ResponseEntity<Usuario> criar(@RequestBody Usuario usuario) {
        Usuario salvo = service.salvar(usuario);
        return ResponseEntity.created(URI.create("/usuarios/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuário por ID")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Optional<Usuario> usuario = service.buscarPorId(id);
        if (usuario.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Usuário com ID " + id + " não encontrado");
        }
        return ResponseEntity.ok(usuario.get());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um usuário")
    public ResponseEntity<Usuario> atualizar(@PathVariable Long id, @RequestBody Usuario usuario) {
        return service.atualizar(id, usuario)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário com ID " + id + " não encontrado"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
