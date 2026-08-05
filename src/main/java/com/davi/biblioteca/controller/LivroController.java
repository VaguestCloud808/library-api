package com.davi.biblioteca.controller;

import com.davi.biblioteca.exception.LivroNaoEncontradoException;
import com.davi.biblioteca.model.Livro;
import com.davi.biblioteca.service.LivroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/livros")
public class LivroController {

    private final LivroService service;

    public LivroController(LivroService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Livro> criar(@RequestBody Livro livro) {
        Livro salvo = service.salvar(livro);
        return ResponseEntity.created(URI.create("/livros/" + salvo.getId())).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<Livro>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarPorId(@PathVariable Long id) {
        Optional<Livro> livro = service.buscarPorId(id);
        if (livro.isEmpty()) {
            throw new LivroNaoEncontradoException("Livro com ID " + id + " não encontrado");
        }
        return ResponseEntity.ok(livro.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizar(@PathVariable Long id, @RequestBody Livro livro) {
        return service.atualizar(id, livro)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro com ID " + id + " não encontrado"));
    }
}
