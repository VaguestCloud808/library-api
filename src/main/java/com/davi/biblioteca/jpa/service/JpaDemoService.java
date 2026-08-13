package com.davi.biblioteca.jpa.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.jpa.entity.LivroEntity;
import com.davi.biblioteca.jpa.repository.LivroJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Demonstra o uso de Spring Data JPA lado a lado com os repositórios JDBC do projeto.
 *
 * Padrão mantido propositalmente:
 *   - JDBC (projeto principal): foco didático, controle total do SQL
 *   - JPA (este service):       foco em produtividade, abstrações automáticas
 *
 * Ambos consomem o mesmo schema `library_db`. A escolha entre JDBC e JPA
 * depende do que a equipe valoriza mais em cada caso.
 */
@Service
public class JpaDemoService {

    private final LivroJpaRepository livroRepository;

    public JpaDemoService(LivroJpaRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    @Transactional(readOnly = true)
    public List<LivroEntity> listarTodos() {
        return livroRepository.findAll();
    }

    @Transactional(readOnly = true)
    public LivroEntity buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        return livroRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Livro com ID " + id + " não encontrado (via JPA)"));
    }

    @Transactional
    public LivroEntity criar(LivroEntity livro) {
        if (livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new DadosInvalidosException("Título não pode ser vazio");
        }
        if (livro.getQuantidadeTotal() == null || livro.getQuantidadeTotal() < 1) {
            throw new DadosInvalidosException("Quantidade total deve ser >= 1");
        }
        if (livro.getQuantidadeDisponivel() == null) {
            livro.setQuantidadeDisponivel(livro.getQuantidadeTotal());
        }
        return livroRepository.save(livro);
    }

    @Transactional
    public void deletar(Long id) {
        if (!livroRepository.existsById(id)) {
            throw new EntidadeNaoEncontradaException("Livro com ID " + id + " não encontrado");
        }
        livroRepository.deleteById(id);
    }
}
