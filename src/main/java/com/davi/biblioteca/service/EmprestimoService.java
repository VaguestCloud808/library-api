package com.davi.biblioteca.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.exception.LivroNaoEncontradoException;
import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.model.Livro;
import com.davi.biblioteca.model.Usuario;
import com.davi.biblioteca.repository.EmprestimoRepository;
import com.davi.biblioteca.repository.LivroRepository;
import com.davi.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmprestimoService {

    private static final int PRAZO_DEVOLUCAO_DIAS = 14;

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    private final UsuarioRepository usuarioRepository;
    private final MultaService multaService;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             LivroRepository livroRepository,
                             UsuarioRepository usuarioRepository,
                             MultaService multaService) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
        this.usuarioRepository = usuarioRepository;
        this.multaService = multaService;
    }

    @Transactional
    public Emprestimo realizar(Long livroId, Long usuarioId) {
        if (livroId == null) {
            throw new DadosInvalidosException("ID do livro não pode ser nulo");
        }
        if (usuarioId == null) {
            throw new DadosInvalidosException("ID do usuário não pode ser nulo");
        }

        Livro livro = livroRepository.buscarPorId(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro com ID " + livroId + " não encontrado"));

        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário com ID " + usuarioId + " não encontrado"));

        if (Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new DadosInvalidosException("Usuário " + usuarioId + " está inativo");
        }

        // decrementa primeiro -> se nao houver exemplar, joga 400 via repository
        livroRepository.decrementarQuantidadeDisponivel(livroId);

        Emprestimo emp = new Emprestimo();
        emp.setLivroId(livroId);
        emp.setUsuarioId(usuarioId);
        emp.setDataEmprestimo(LocalDateTime.now());
        emp.setDataDevolucaoPrevista(LocalDateTime.now().plusDays(PRAZO_DEVOLUCAO_DIAS));
        return emprestimoRepository.salvar(emp);
    }

    @Transactional
    public Emprestimo devolver(Long emprestimoId) {
        if (emprestimoId == null) {
            throw new DadosInvalidosException("ID do empréstimo não pode ser nulo");
        }
        Emprestimo existente = emprestimoRepository.buscarPorId(emprestimoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Empréstimo com ID " + emprestimoId + " não encontrado"));

        if (existente.isDevolvido()) {
            throw new DadosInvalidosException("Empréstimo " + emprestimoId + " já foi devolvido");
        }

        LocalDateTime agora = LocalDateTime.now();
        emprestimoRepository.registrarDevolucao(emprestimoId, agora);
        livroRepository.incrementarQuantidadeDisponivel(existente.getLivroId());
        multaService.gerarMultaSeAtrasado(existente, agora);

        existente.setDataDevolucaoReal(agora);
        return existente;
    }

    public Optional<Emprestimo> buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        return emprestimoRepository.buscarPorId(id);
    }

    public List<Emprestimo> listar() {
        return emprestimoRepository.listar();
    }
}
