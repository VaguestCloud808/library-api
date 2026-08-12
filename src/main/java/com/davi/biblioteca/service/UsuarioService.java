package com.davi.biblioteca.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.model.Usuario;
import com.davi.biblioteca.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario salvar(Usuario usuario) {
        validar(usuario);
        if (usuario.getAtivo() == null) {
            usuario.setAtivo(Boolean.TRUE);
        }
        if (usuario.getDataCadastro() == null) {
            usuario.setDataCadastro(LocalDateTime.now());
        }
        return repository.salvar(usuario);
    }

    public List<Usuario> listar() {
        return repository.listar();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        return repository.buscarPorId(id);
    }

    public Optional<Usuario> atualizar(Long id, Usuario usuario) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        if (repository.buscarPorId(id).isEmpty()) {
            throw new EntidadeNaoEncontradaException("Usuário com ID " + id + " não encontrado");
        }
        validar(usuario);
        usuario.setId(id);
        repository.atualizar(usuario);
        return repository.buscarPorId(id);
    }

    public void deletar(Long id) {
        if (id == null) {
            throw new DadosInvalidosException("ID não pode ser nulo");
        }
        if (repository.buscarPorId(id).isEmpty()) {
            throw new EntidadeNaoEncontradaException("Usuário com ID " + id + " não encontrado");
        }
        repository.deletar(id);
    }

    private void validar(Usuario u) {
        if (u.getNome() == null || u.getNome().isBlank()) {
            throw new DadosInvalidosException("Nome não pode ser vazio");
        }
        if (u.getEmail() == null || u.getEmail().isBlank()) {
            throw new DadosInvalidosException("Email não pode ser vazio");
        }
        if (!u.getEmail().contains("@")) {
            throw new DadosInvalidosException("Email inválido");
        }
        if (u.getCpf() == null || u.getCpf().isBlank()) {
            throw new DadosInvalidosException("CPF não pode ser vazio");
        }
    }
}
