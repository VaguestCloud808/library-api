package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Emprestimo;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class EmprestimoRepositoryJdbc implements EmprestimoRepository {

    private final DataSource dataSource;

    public EmprestimoRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Emprestimo salvar(Emprestimo e) {
        String sql = "INSERT INTO emprestimo (livro_id, usuario_id, data_emprestimo, data_devolucao_prevista) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, e.getLivroId());
            ps.setLong(2, e.getUsuarioId());
            ps.setTimestamp(3, Timestamp.valueOf(e.getDataEmprestimo()));
            ps.setTimestamp(4, Timestamp.valueOf(e.getDataDevolucaoPrevista()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getLong(1));
                }
            }
            return e;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao salvar emprestimo: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<Emprestimo> buscarPorId(Long id) {
        String sql = "SELECT id, livro_id, usuario_id, data_emprestimo, data_devolucao_prevista, data_devolucao_real "
                + "FROM emprestimo WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar emprestimo por id", ex);
        }
    }

    @Override
    public List<Emprestimo> listar() {
        String sql = "SELECT id, livro_id, usuario_id, data_emprestimo, data_devolucao_prevista, data_devolucao_real FROM emprestimo";
        List<Emprestimo> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar emprestimos", ex);
        }
    }

    @Override
    public void registrarDevolucao(Long id, LocalDateTime dataDevolucao) {
        String sql = "UPDATE emprestimo SET data_devolucao_real = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dataDevolucao));
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao registrar devolucao: " + ex.getMessage(), ex);
        }
    }

    private Emprestimo mapear(ResultSet rs) throws SQLException {
        Emprestimo e = new Emprestimo();
        e.setId(rs.getLong("id"));
        e.setLivroId(rs.getLong("livro_id"));
        e.setUsuarioId(rs.getLong("usuario_id"));
        e.setDataEmprestimo(rs.getTimestamp("data_emprestimo").toLocalDateTime());
        e.setDataDevolucaoPrevista(rs.getTimestamp("data_devolucao_prevista").toLocalDateTime());
        Timestamp dr = rs.getTimestamp("data_devolucao_real");
        e.setDataDevolucaoReal(dr == null ? null : dr.toLocalDateTime());
        return e;
    }
}
