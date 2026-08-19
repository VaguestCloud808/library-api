package com.davi.biblioteca.controller;

import com.davi.biblioteca.exception.LivroNaoEncontradoException;
import com.davi.biblioteca.model.Livro;
import com.davi.biblioteca.service.LivroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/livros")
@Tag(name = "Livro", description = "Endpoints do módulo de livros (JDBC)")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria um livro", description = "Cadastra um novo livro na biblioteca. ISBN deve ser único.")
    public ResponseEntity<Livro> criar(@RequestBody Livro livro) {
        Livro salvo = service.salvar(livro);
        return ResponseEntity.created(URI.create("/livros/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    @Operation(summary = "Lista todos os livros")
    public ResponseEntity<List<Livro>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um livro por ID")
    public ResponseEntity<Livro> buscarPorId(@PathVariable Long id) {
        Optional<Livro> livro = service.buscarPorId(id);
        if (livro.isEmpty()) {
            throw new LivroNaoEncontradoException("Livro com ID " + id + " não encontrado");
        }
        return ResponseEntity.ok(livro.get());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um livro", description = "Atualiza os dados cadastrais do livro. NÃO altera quantidade_disponivel (regra de negócio).")
    public ResponseEntity<Livro> atualizar(@PathVariable Long id, @RequestBody Livro livro) {
        return service.atualizar(id, livro)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro com ID " + id + " não encontrado"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um livro", description = "Deleta o livro do cadastro. Retorna 204 em sucesso ou 404 se não existir.")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
