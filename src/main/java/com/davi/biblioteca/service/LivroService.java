package com.davi.biblioteca.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.LivroNaoEncontradoException;
import com.davi.biblioteca.model.Livro;
import com.davi.biblioteca.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LivroService {

    private final LivroRepository repository;

    public LivroService(LivroRepository repository) {
        this.repository = repository;
    }

    public Livro salvar(Livro livro) {
        validar(livro);
        if (livro.getDataCadastro() == null) {
            livro.setDataCadastro(LocalDateTime.now());
        }
        repository.salvar(livro);
        return livro;
    }

    public List<Livro> listar() {
        return repository.listar();
    }

    public Optional<Livro> buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        return repository.buscarPorId(id);
    }

    public void deletar(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        if (!repository.buscarPorId(id).isPresent()) {
            throw new LivroNaoEncontradoException("Livro com ID " + id + " não encontrado");
        }
        repository.deletar(id);
    }

    public Optional<Livro> atualizar(Long id, Livro livro) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        Optional<Livro> existente = repository.buscarPorId(id);
        if (existente.isEmpty()) {
            throw new LivroNaoEncontradoException("Livro com ID " + id + " não encontrado");
        }
        validar(livro);
        livro.setId(id);
        repository.atualizar(livro);
        return repository.buscarPorId(id);
    }

    private void validar(Livro livro) {
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new DadosInvalidosException("Título não pode ser vazio");
        }
        if (livro.getAutor() == null || livro.getAutor().isBlank()) {
            throw new DadosInvalidosException("Autor não pode ser vazio");
        }
        if (livro.getIsbn() == null || livro.getIsbn().isBlank()) {
            throw new DadosInvalidosException("ISBN não pode ser vazio");
        }
        if (livro.getQuantidadeTotal() == null || livro.getQuantidadeTotal() < 1) {
            throw new DadosInvalidosException("Quantidade total deve ser no mínimo 1");
        }
        if (livro.getQuantidadeDisponivel() == null || livro.getQuantidadeDisponivel() < 0) {
            throw new DadosInvalidosException("Quantidade disponível não pode ser negativa");
        }
        if (livro.getQuantidadeDisponivel() > livro.getQuantidadeTotal()) {
            throw new DadosInvalidosException("Quantidade disponível não pode ser maior que a total");
        }
    }
}
