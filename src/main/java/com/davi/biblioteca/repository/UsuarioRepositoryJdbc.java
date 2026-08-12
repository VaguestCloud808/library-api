package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Usuario;
import com.davi.biblioteca.exception.DadosInvalidosException;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepositoryJdbc implements UsuarioRepository {

    private final DataSource dataSource;

    public UsuarioRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, email, cpf, telefone, endereco, cidade, estado, cep, data_nascimento, ativo, data_cadastro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getCpf());
            stmt.setString(4, usuario.getTelefone());
            stmt.setString(5, usuario.getEndereco());
            stmt.setString(6, usuario.getCidade());
            stmt.setString(7, usuario.getEstado());
            stmt.setString(8, usuario.getCep());
            stmt.setDate(9, usuario.getDataNascimento() == null ? null : Date.valueOf(usuario.getDataNascimento()));
            stmt.setBoolean(10, usuario.getAtivo() == null ? Boolean.TRUE : usuario.getAtivo());
            stmt.setTimestamp(11, Timestamp.valueOf(usuario.getDataCadastro()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }
            return usuario;

        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                throw new DadosInvalidosException("Email ou CPF já cadastrado");
            }
            throw new RuntimeException("Erro ao salvar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Usuario> listar() {
        String sql = "SELECT id, nome, email, cpf, telefone, endereco, cidade, estado, cep, data_nascimento, ativo, data_cadastro "
                + "FROM usuario";

        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearResultSet(rs));
            }
            return usuarios;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar usuarios", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        String sql = "SELECT id, nome, email, cpf, telefone, endereco, cidade, estado, cep, data_nascimento, ativo, data_cadastro "
                + "FROM usuario WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearResultSet(rs));
                }
                return Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar usuario por id", e);
        }
    }

    @Override
    public void atualizar(Usuario usuario) {
        String sql = "UPDATE usuario SET nome = ?, email = ?, cpf = ?, telefone = ?, endereco = ?, cidade = ?, "
                + "estado = ?, cep = ?, data_nascimento = ?, ativo = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.setString(3, usuario.getCpf());
            ps.setString(4, usuario.getTelefone());
            ps.setString(5, usuario.getEndereco());
            ps.setString(6, usuario.getCidade());
            ps.setString(7, usuario.getEstado());
            ps.setString(8, usuario.getCep());
            ps.setDate(9, usuario.getDataNascimento() == null ? null : Date.valueOf(usuario.getDataNascimento()));
            ps.setBoolean(10, usuario.getAtivo() == null ? Boolean.TRUE : usuario.getAtivo());
            ps.setLong(11, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                throw new DadosInvalidosException("Email ou CPF já cadastrado");
            }
            throw new RuntimeException("Erro ao atualizar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deletar(Long id) {
        String sql = "DELETE FROM usuario WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar usuario: " + e.getMessage(), e);
        }
    }

    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getLong("id"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setCpf(rs.getString("cpf"));
        u.setTelefone(rs.getString("telefone"));
        u.setEndereco(rs.getString("endereco"));
        u.setCidade(rs.getString("cidade"));
        u.setEstado(rs.getString("estado"));
        u.setCep(rs.getString("cep"));
        Date dn = rs.getDate("data_nascimento");
        u.setDataNascimento(dn == null ? null : dn.toLocalDate());
        u.setAtivo(rs.getBoolean("ativo"));
        Timestamp dc = rs.getTimestamp("data_cadastro");
        u.setDataCadastro(dc == null ? null : dc.toLocalDateTime());
        return u;
    }
}
