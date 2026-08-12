package com.davi.biblioteca.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Multa {

    private Long id;
    private Long emprestimoId;
    private BigDecimal valor;
    private Integer diasAtraso;
    private LocalDateTime dataGeracao;
    private Boolean paga;
    private LocalDateTime dataPagamento;

    public Multa() {
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