package com.davi.biblioteca.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "multa")
public class MultaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emprestimo_id", nullable = false, unique = true)
    private Long emprestimoId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(name = "dias_atraso", nullable = false)
    private Integer diasAtraso;

    @Column(name = "data_geracao", nullable = false, updatable = false, insertable = false)
    private LocalDateTime dataGeracao;

    @Column(nullable = false)
    private Boolean paga;

    @Column(name = "data_pagamento")
    private LocalDateTime dataPagamento;

    public MultaEntity() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmprestimoId() { return emprestimoId; }
    public void setEmprestimoId(Long emprestimoId) { this.emprestimoId = emprestimoId; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Integer getDiasAtraso() { return diasAtraso; }
    public void setDiasAtraso(Integer diasAtraso) { this.diasAtraso = diasAtraso; }

    public LocalDateTime getDataGeracao() { return dataGeracao; }
    public void setDataGeracao(LocalDateTime dataGeracao) { this.dataGeracao = dataGeracao; }

    public Boolean getPaga() { return paga; }
    public void setPaga(Boolean paga) { this.paga = paga; }

    public LocalDateTime getDataPagamento() { return dataPagamento; }
    public void setDataPagamento(LocalDateTime dataPagamento) { this.dataPagamento = dataPagamento; }
}
