package com.davi.biblioteca.jpa.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.jpa.entity.UsuarioEntity;
import com.davi.biblioteca.jpa.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Versão JPA (demonstrativa) do {@link com.davi.biblioteca.service.UsuarioService}.
 *
 * Convivência com o JDBC: ambos consomem o mesmo schema library_db. O JPA é
 * mais conciso (save/findById derivados), enquanto o JDBC é mais explícito.
 */
@Service
public class UsuarioJpaService {

    private final UsuarioJpaRepository repository;

    public UsuarioJpaService(UsuarioJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioEntity> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public UsuarioEntity buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Usuário com ID " + id + " não encontrado (via JPA)"));
    }

    @Transactional
    public UsuarioEntity criar(UsuarioEntity usuario) {
        validar(usuario);
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(Boolean.TRUE);
        }
        if (usuario.getDataCadastro() == null) {
            usuario.setDataCadastro(LocalDateTime.now());
        }
        if (repository.existsByEmail(usuario.getEmail())) {
            throw new DadosInvalidosException("Email já cadastrado: " + usuario.getEmail());
        }
        if (repository.existsByCpf(usuario.getCpf())) {
            throw new DadosInvalidosException("CPF já cadastrado: " + usuario.getCpf());
        }
        return repository.save(usuario);
    }

    @Transactional
    public UsuarioEntity atualizar(Long id, UsuarioEntity usuario) {
        UsuarioEntity existente = buscarPorId(id);
        validar(usuario);
        usuario.setId(id);
        usuario.setDataCadastro(existente.getDataCadastro());
        return repository.save(usuario);
    }

    @Transactional
    public void deletar(Long id) {
        buscarPorId(id);
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioEntity> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    private void validar(UsuarioEntity u) {
        if (u.getNome() == null || u.getNome().isBlank()) {
            throw new DadosInvalidosException("Nome não pode ser vazio");
        }
        if (u.getEmail() == null || u.getEmail().isBlank() || !u.getEmail().contains("@")) {
            throw new DadosInvalidosException("Email inválido");
        }
        if (u.getCpf() == null || u.getCpf().isBlank()) {
            throw new DadosInvalidosException("CPF não pode ser vazio");
        }
    }
}
