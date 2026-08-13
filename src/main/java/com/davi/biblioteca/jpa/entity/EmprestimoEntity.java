package com.davi.biblioteca.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "emprestimo")
public class EmprestimoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "livro_id", nullable = false)
    private Long livroId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "data_emprestimo", nullable = false)
    private LocalDateTime dataEmprestimo;

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDateTime dataDevolucaoPrevista;

    @Column(name = "data_devolucao_real")
    private LocalDateTime dataDevolucaoReal;

    public EmprestimoEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLivroId() { return livroId; }
    public void setLivroId(Long livroId) { this.livroId = livroId; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(LocalDateTime dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }

    public LocalDateTime getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public void setDataDevolucaoPrevista(LocalDateTime dataDevolucaoPrevista) { this.dataDevolucaoPrevista = dataDevolucaoPrevista; }

    public LocalDateTime getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDateTime dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }
}
