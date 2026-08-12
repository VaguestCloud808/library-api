-- Etapa 6 - Módulo Multas
-- Tabela para persistir multas por atraso em empréstimos.
-- Idempotência garantida por UNIQUE(emprestimo_id).

USE library_db;

CREATE TABLE IF NOT EXISTS multa (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    emprestimo_id   BIGINT NOT NULL,
    valor           DECIMAL(10,2) NOT NULL,
    dias_atraso     INT NOT NULL,
    data_geracao    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paga            BOOLEAN NOT NULL DEFAULT FALSE,
    data_pagamento  DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_multa_emprestimo (emprestimo_id),
    CONSTRAINT fk_multa_emprestimo FOREIGN KEY (emprestimo_id)
        REFERENCES emprestimo(id),
    CONSTRAINT ck_multa_valor_positivo CHECK (valor >= 0),
    CONSTRAINT ck_multa_dias CHECK (dias_atraso >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;