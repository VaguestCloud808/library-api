package com.davi.biblioteca.jpa.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.jpa.entity.EmprestimoEntity;
import com.davi.biblioteca.jpa.entity.LivroEntity;
import com.davi.biblioteca.jpa.entity.UsuarioEntity;
import com.davi.biblioteca.jpa.repository.EmprestimoJpaRepository;
import com.davi.biblioteca.jpa.repository.LivroJpaRepository;
import com.davi.biblioteca.jpa.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Versão JPA (demonstrativa) do {@link com.davi.biblioteca.service.EmprestimoService}.
 *
 * Note como o @Transactional "abraça" toda a operação atômica (decrementa
 * quantidade + insere empréstimo), assim como no JDBC. A diferença é que aqui
 * não escrevemos SQL — o Hibernate gera.
 */
@Service
public class EmprestimoJpaService {

    private static final int PRAZO_DEVOLUCAO_DIAS = 14;

    private final EmprestimoJpaRepository emprestimoRepository;
    private final LivroJpaRepository livroRepository;
    private final UsuarioJpaRepository usuarioRepository;

    public EmprestimoJpaService(EmprestimoJpaRepository emprestimoRepository,
                                LivroJpaRepository livroRepository,
                                UsuarioJpaRepository usuarioRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<EmprestimoEntity> listarTodos() {
        return emprestimoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public EmprestimoEntity buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Empréstimo com ID " + id + " não encontrado (via JPA)"));
    }

    @Transactional
    public EmprestimoEntity realizar(Long livroId, Long usuarioId) {
        if (livroId == null) {
            throw new DadosInvalidosException("ID do livro não pode ser nulo");
        }
        if (usuarioId == null) {
            throw new DadosInvalidosException("ID do usuário não pode ser nulo");
        }

        LivroEntity livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Livro com ID " + livroId + " não encontrado"));

        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Usuário com ID " + usuarioId + " não encontrado"));

        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new DadosInvalidosException("Usuário " + usuarioId + " está inativo");
        }

        // @Modifying: se retornar 0, ninguém foi decrementado → sem exemplares
        int atualizado = livroRepository.decrementarQuantidadeDisponivel(livroId);
        if (atualizado == 0) {
            throw new DadosInvalidosException("Sem exemplares disponíveis para o livro " + livroId);
        }

        EmprestimoEntity emp = new EmprestimoEntity();
        emp.setLivroId(livroId);
        emp.setUsuarioId(usuarioId);
        emp.setDataEmprestimo(LocalDateTime.now());
        emp.setDataDevolucaoPrevista(LocalDateTime.now().plusDays(PRAZO_DEVOLUCAO_DIAS));
        return emprestimoRepository.save(emp);
    }

    @Transactional
    public EmprestimoEntity devolver(Long emprestimoId) {
        EmprestimoEntity existente = buscarPorId(emprestimoId);
        if (existente.getDataDevolucaoReal() != null) {
            throw new DadosInvalidosException("Empréstimo " + emprestimoId + " já foi devolvido");
        }
        LocalDateTime agora = LocalDateTime.now();
        emprestimoRepository.registrarDevolucao(emprestimoId, agora);
        livroRepository.incrementarQuantidadeDisponivel(existente.getLivroId());
        existente.setDataDevolucaoReal(agora);
        return existente;
    }
}
