package com.davi.biblioteca.repository;

import com.davi.biblioteca.model.Multa;
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
public class MultaRepositoryJdbc implements MultaRepository {

    private final DataSource dataSource;

    public MultaRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Multa salvar(Multa m) {
        String sql = "INSERT INTO multa (emprestimo_id, valor, dias_atraso, paga, data_pagamento) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.getEmprestimoId());
            ps.setBigDecimal(2, m.getValor());
            ps.setInt(3, m.getDiasAtraso());
            ps.setBoolean(4, Boolean.TRUE.equals(m.getPaga()));
            if (m.getDataPagamento() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(m.getDataPagamento()));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setId(rs.getLong(1));
                }
            }
            return m;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao salvar multa: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<Multa> buscarPorId(Long id) {
        String sql = "SELECT id, emprestimo_id, valor, dias_atraso, data_geracao, paga, data_pagamento FROM multa WHERE id = ?";
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
            throw new RuntimeException("Erro ao buscar multa por id", ex);
        }
    }

    @Override
    public Optional<Multa> buscarPorEmprestimoId(Long emprestimoId) {
        String sql = "SELECT id, emprestimo_id, valor, dias_atraso, data_geracao, paga, data_pagamento FROM multa WHERE emprestimo_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, emprestimoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao buscar multa por emprestimo", ex);
        }
    }

    @Override
    public boolean existePorEmprestimoId(Long emprestimoId) {
        String sql = "SELECT 1 FROM multa WHERE emprestimo_id = ? LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, emprestimoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao verificar multa por emprestimo", ex);
        }
    }

    @Override
    public List<Multa> listar() {
        String sql = "SELECT id, emprestimo_id, valor, dias_atraso, data_geracao, paga, data_pagamento FROM multa ORDER BY data_geracao DESC";
        List<Multa> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar multas", ex);
        }
    }

    @Override
    public List<Multa> listarEmAberto() {
        String sql = "SELECT m.id, m.emprestimo_id, m.valor, m.dias_atraso, m.data_geracao, m.paga, m.data_pagamento " +
                "FROM multa m JOIN emprestimo e ON e.id = m.emprestimo_id " +
                "WHERE m.paga = FALSE AND e.data_devolucao_real IS NULL " +
                "ORDER BY m.data_geracao DESC";
        List<Multa> lista = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao listar multas em aberto", ex);
        }
    }

    @Override
    public Multa marcarComoPaga(Long id) {
        String sql = "UPDATE multa SET paga = TRUE, data_pagamento = ? WHERE id = ? AND paga = FALSE";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);
            ps.executeUpdate();
            return buscarPorId(id).orElseThrow(() ->
                    new RuntimeException("Multa " + id + " não encontrada após atualização"));
        } catch (SQLException ex) {
            throw new RuntimeException("Erro ao pagar multa: " + ex.getMessage(), ex);
        }
    }

    private Multa mapear(ResultSet rs) throws SQLException {
        Multa m = new Multa();
        m.setId(rs.getLong("id"));
        m.setEmprestimoId(rs.getLong("emprestimo_id"));
        m.setValor(rs.getBigDecimal("valor"));
        m.setDiasAtraso(rs.getInt("dias_atraso"));
        m.setDataGeracao(rs.getTimestamp("data_geracao").toLocalDateTime());
        m.setPaga(rs.getBoolean("paga"));
        Timestamp dp = rs.getTimestamp("data_pagamento");
        m.setDataPagamento(dp == null ? null : dp.toLocalDateTime());
        return m;
    }
}