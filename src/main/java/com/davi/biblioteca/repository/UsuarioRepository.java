package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {

    Usuario salvar(Usuario usuario);

    List<Usuario> listar();

    Optional<Usuario> buscarPorId(Long id);

    void atualizar(Usuario usuario);

    boolean deletar(Long id);
}
