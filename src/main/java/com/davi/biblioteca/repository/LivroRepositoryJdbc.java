package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Livro;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class LivroRepositoryJdbc implements LivroRepository {

    private final DataSource dataSource;

    public LivroRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Livro salvar(Livro livro) {
        String sql = "INSERT INTO livro (titulo, autor, isbn, quantidade_total, quantidade_disponivel, data_cadastro) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setString(3, livro.getIsbn());
            stmt.setInt(4, livro.getQuantidadeTotal());
            stmt.setInt(5, livro.getQuantidadeDisponivel());
            stmt.setTimestamp(6, Timestamp.valueOf(livro.getDataCadastro()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    livro.setId(rs.getLong(1));
                }
            }
            return livro;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar livro", e);
        }
    }

    @Override
    public List<Livro> listar() {
        String sql = "SELECT id, titulo, autor, isbn, quantidade_total, quantidade_disponivel, data_cadastro "
                + "FROM livro";

        List<Livro> livros = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                livros.add(mapearResultSet(rs));
            }
            return livros;

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar livros", e);
        }
    }

    @Override
    public Optional<Livro> buscarPorId(Long id) {
        String sql = "SELECT id, titulo, autor, isbn, quantidade_total, quantidade_disponivel, data_cadastro "
                + "FROM livro WHERE id = ?";

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
            throw new RuntimeException("Erro ao buscar livro por id", e);
        }
    }

    @Override
    public void atualizar(Livro livro) {
        String sql = "UPDATE livro SET titulo = ?, autor = ?, isbn = ?, quantidade_total = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            ps.setInt(4, livro.getQuantidadeTotal());
            ps.setLong(5, livro.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar livro: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deletar(Long id) {
        String sql = "DELETE FROM livro WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar livro: " + e.getMessage(), e);
        }
    }

    private Livro mapearResultSet(ResultSet rs) throws SQLException {
        Livro livro = new Livro();
        livro.setId(rs.getLong("id"));
        livro.setTitulo(rs.getString("titulo"));
        livro.setAutor(rs.getString("autor"));
        livro.setIsbn(rs.getString("isbn"));
        livro.setQuantidadeTotal(rs.getInt("quantidade_total"));
        livro.setQuantidadeDisponivel(rs.getInt("quantidade_disponivel"));
        livro.setDataCadastro(rs.getTimestamp("data_cadastro").toLocalDateTime());
        return livro;
    }
}
