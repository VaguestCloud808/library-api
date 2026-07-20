# Library API

API REST para gerenciamento de biblioteca, desenvolvida como projeto de aprendizado e portfólio em Java + Spring Boot.

## Sobre o projeto

Sistema backend que permite controlar livros, usuários, empréstimos e multas de uma biblioteca.
Projeto em evolução: começou com JDBC puro para fins didáticos e será migrado para Spring Data JPA nas próximas etapas.

## Tecnologias

- Java 21 (LTS)
- Spring Boot 4.0.7
- Maven (via Maven Wrapper)
- MySQL
- JDBC puro (etapa atual)
- Git + GitHub

## Estrutura do projeto

src/
├── main/
│   ├── java/com/davi/biblioteca/libraryapi/
│   │   └── LibraryApiApplication.java
│   └── resources/
│       └── application.properties
└── test/
└── java/com/davi/biblioteca/libraryapi/
└── LibraryApiApplicationTests.java

Arquitetura em camadas (em construção):
Controller → Service → Repository → Database

## Como rodar o projeto

### Pré-requisitos
- Java 21 instalado
- MySQL instalado e rodando
- Git (opcional, só para clonar)

### Passos
1. Clonar o repositório
   git clone https://github.com/VaguestCloud808/library-api.git
2. Entrar na pasta do projeto
   cd library-api
3. Rodar a aplicação
   ./mvnw spring-boot:run
4. Acessar no navegador
   http://localhost:8080

A aplicação sobe na porta 8080 por padrão.

## Status do projeto

- [x] Ambiente validado (Java, Maven)
- [x] Estrutura inicial criada via Spring Initializr
- [x] Repositório no GitHub
- [ ] Módulo de Livros (CRUD)
- [ ] Módulo de Usuários (CRUD)
- [ ] Módulo de Empréstimos
- [ ] Módulo de Multas
- [ ] Migração para Spring Data JPA
- [ ] Documentação Swagger/OpenAPI
- [ ] Testes automatizados
- [ ] Containerização com Docker
- [ ] Deploy em nuvem

## Objetivos de aprendizado

- Consolidar Java e Orientação a Objetos
- Dominar Spring Boot e o ecossistema (JPA, Security, testes)
- Aprender MySQL e modelagem de dados
- Construir projeto profissional para portfólio
- Desenvolver autonomia para criar APIs sem dependência de IA

## Autor

Davi Alves Couto
GitHub: https://github.com/VaguestCloud808