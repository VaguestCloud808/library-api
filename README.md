# Library API

API REST para gerenciamento de biblioteca, desenvolvida como projeto de aprendizado e portfólio em Java + Spring Boot.

## Sobre o projeto

Sistema backend que controla livros, usuários, empréstimos e multas de uma biblioteca.
O projeto começou com JDBC puro para entender o que o framework abstrai e foi evoluindo em camadas.

## Tecnologias

- Java 21 (LTS)
- Spring Boot 4.0.7
- Maven (via Maven Wrapper)
- MySQL
- JDBC puro
- Spring Scheduling (jobs em background)

## Estrutura do projeto

```
src/main/java/com/davi/biblioteca/
├── model/          POJOs (Livro, Emprestimo, Usuario, Multa)
├── repository/     Interfaces + impl JDBC
├── service/        Regras de negócio
├── scheduler/      Jobs @Scheduled
├── controller/     Endpoints REST
├── exception/      Exceções + ApiExceptionHandler
└── config/         Configuração do DataSource
```

Arquitetura: Controller → Service → Repository → Database.

## Como rodar

### Pré-requisitos

- Java 21
- MySQL 8 rodando em `localhost:3306`
- Variável de ambiente `MYSQL_PASSWORD` com a senha do root

### Setup

```bash
# 1. Criar schema (rodar uma vez)
mysql -u root -p library_db < scripts-sql/03_create_table_multa.sql

# 2. Subir a aplicação
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Endpoints

### Livros
- `GET    /livros` — lista
- `GET    /livros/{id}` — busca por id
- `POST   /livros` — cria
- `PUT    /livros/{id}` — atualiza (não altera `quantidadeDisponivel`)
- `DELETE /livros/{id}` — remove

### Usuários
- `GET    /usuarios` — lista
- `GET    /usuarios/{id}` — busca por id
- `POST   /usuarios` — cria
- `PUT    /usuarios/{id}` — atualiza
- `DELETE /usuarios/{id}` — remove

### Empréstimos
- `POST   /emprestimos` — registra empréstimo (body: `{livroId, usuarioId}`)
- `GET    /emprestimos` — lista
- `GET    /emprestimos/{id}` — busca por id
- `POST   /emprestimos/{id}/devolucao` — registra devolução (gera multa se houver atraso)

### Multas
- `GET    /emprestimos/{id}/multa` — multa do empréstimo (prevista ou persistida)
- `GET    /multas` — lista todas as multas
- `GET    /multas?atrasadas=true` — só multas em aberto
- `POST   /multas/{id}/pagar` — marca multa como paga
- `POST   /multas/scheduler/disparar` — força execução do scheduler (uso em testes)

## Regras de negócio

- **Prazo de empréstimo:** 14 dias
- **Multa por atraso:** R$ 1,50 por dia (constante em `MultaService.VALOR_MULTA_POR_DIA`)
- **Idempotência de multa:** cada empréstimo gera no máximo uma multa. Garantida por:
  1. Check no service (`MultaService.gerarMultaNovaSeAtrasado`)
  2. Constraint `UNIQUE(emprestimo_id)` na tabela
- **Scheduler diário:** todo dia às 2h varre empréstimos atrasados não devolvidos e gera multas automaticamente

## Status do projeto

- [x] Ambiente e setup
- [x] Módulo de Livros (CRUD)
- [x] Módulo de Usuários (CRUD)
- [x] Módulo de Empréstimos
- [x] Módulo de Multas + scheduler
- [x] Tratamento global de erros (`@RestControllerAdvice`)
- [ ] Migração para Spring Data JPA
- [ ] Documentação Swagger/OpenAPI
- [ ] Testes automatizados
- [ ] Containerização com Docker
- [ ] Deploy em nuvem

## Objetivos de aprendizado

- Consolidar Java e Orientação a Objetos
- Dominar Spring Boot (core, scheduling, exception handling)
- Aprender MySQL e modelagem relacional (constraints, FK, UNIQUE)
- Construir projeto profissional para portfólio
- Desenvolver autonomia para criar APIs sem dependência de IA

## Autor

Davi Alves Couto — [github.com/VaguestCloud808](https://github.com/VaguestCloud808)