package com.davi.biblioteca.service;

import com.davi.biblioteca.exception.DadosInvalidosException;
import com.davi.biblioteca.exception.EntidadeNaoEncontradaException;
import com.davi.biblioteca.exception.LivroNaoEncontradoException;
import com.davi.biblioteca.model.Emprestimo;
import com.davi.biblioteca.model.Livro;
import com.davi.biblioteca.model.Usuario;
import com.davi.biblioteca.repository.EmprestimoRepository;
import com.davi.biblioteca.repository.LivroRepository;
import com.davi.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmprestimoServiceTest {

    private EmprestimoRepository emprestimoRepository;
    private LivroRepository livroRepository;
    private UsuarioRepository usuarioRepository;
    private MultaService multaService;
    private EmprestimoService emprestimoService;

    @BeforeEach
    void setUp() {
        // cria 4 dublês (3 repositories + 1 service auxiliar)
        emprestimoRepository = mock(EmprestimoRepository.class);
        livroRepository = mock(LivroRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        multaService = mock(MultaService.class);
        // instancia o service real passando os dublês
        emprestimoService = new EmprestimoService(
                emprestimoRepository, livroRepository, usuarioRepository, multaService);
    }

    // ========== realizar(): regras de validação ==========

    @Test
    @DisplayName("realizar deve lançar DadosInvalidosException quando livroId é nulo")
    void realizar_lancaExcecaoQuandoLivroIdNulo() {
        assertThrows(DadosInvalidosException.class,
                () -> emprestimoService.realizar(null, 1L));
        // garante que nem chegou a chamar o repository
        verify(livroRepository, never()).buscarPorId(anyLong());
    }

    @Test
    @DisplayName("realizar deve lançar LivroNaoEncontradoException quando livro não existe")
    void realizar_lancaExcecaoQuandoLivroNaoExiste() {
        // dublê: quando pedirem o livro 99, retorna vazio
        when(livroRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class,
                () -> emprestimoService.realizar(99L, 1L));
    }

    @Test
    @DisplayName("realizar deve lançar DadosInvalidosException quando usuário está inativo")
    void realizar_lancaExcecaoQuandoUsuarioInativo() {
        // livro existe
        when(livroRepository.buscarPorId(1L)).thenReturn(Optional.of(livro(1L, "Clean Code", 3)));
        // usuário existe MAS está inativo
        Usuario inativo = usuario(1L, "Davi");
        inativo.setAtivo(false);
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(inativo));

        assertThrows(DadosInvalidosException.class,
                () -> emprestimoService.realizar(1L, 1L));

        // garante que NÃO decrementou (regra falhou antes)
        verify(livroRepository, never()).decrementarQuantidadeDisponivel(anyLong());
        verify(emprestimoRepository, never()).salvar(any(Emprestimo.class));
    }

    @Test
    @DisplayName("realizar deve lançar DadosInvalidosException quando livro está sem exemplares (regra do repository)")
    void realizar_lancaExcecaoQuandoLivroSemExemplares() {
        when(livroRepository.buscarPorId(1L)).thenReturn(Optional.of(livro(1L, "Clean Code", 0)));
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(usuario(1L, "Davi")));

        // dublê do repository: simula o caso "0 linhas afetadas" lançando a exceção
        doThrow(new DadosInvalidosException("Livro com ID 1 sem exemplares disponiveis"))
                .when(livroRepository).decrementarQuantidadeDisponivel(1L);

        assertThrows(DadosInvalidosException.class,
                () -> emprestimoService.realizar(1L, 1L));

        // garante que NÃO salvou empréstimo (regra falhou antes)
        verify(emprestimoRepository, never()).salvar(any(Emprestimo.class));
    }

    @Test
    @DisplayName("realizar deve salvar empréstimo com prazo de 14 dias quando tudo válido")
    void realizar_salvaEmprestimoComPrazo14Dias() {
        when(livroRepository.buscarPorId(1L)).thenReturn(Optional.of(livro(1L, "Clean Code", 3)));
        when(usuarioRepository.buscarPorId(1L)).thenReturn(Optional.of(usuario(1L, "Davi")));
        // dublê do repository: faz nada quando decrementar (simula sucesso)
        doNothing().when(livroRepository).decrementarQuantidadeDisponivel(1L);
        // quando salvar, retorna o que foi passado
        when(emprestimoRepository.salvar(any(Emprestimo.class))).thenAnswer(inv -> inv.getArgument(0));

        Emprestimo resultado = emprestimoService.realizar(1L, 1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getLivroId());
        assertEquals(1L, resultado.getUsuarioId());
        assertNotNull(resultado.getDataEmprestimo(), "dataEmprestimo deve ser preenchida");
        assertNotNull(resultado.getDataDevolucaoPrevista(), "dataDevolucaoPrevista deve ser preenchida");
        // confere que data prevista = data empréstimo + 14 dias
        assertEquals(
                resultado.getDataEmprestimo().plusDays(14).toLocalDate(),
                resultado.getDataDevolucaoPrevista().toLocalDate()
        );
        verify(livroRepository).decrementarQuantidadeDisponivel(1L);
    }

    // ========== devolver(): regras de validação ==========

    @Test
    @DisplayName("devolver deve lançar DadosInvalidosException quando empréstimo já foi devolvido")
    void devolver_lancaExcecaoQuandoJaDevolvido() {
        Emprestimo emp = emprestimo(1L);
        emp.setDataDevolucaoReal(LocalDateTime.of(2026, 1, 20, 10, 0)); // já devolvido
        when(emprestimoRepository.buscarPorId(1L)).thenReturn(Optional.of(emp));

        assertThrows(DadosInvalidosException.class,
                () -> emprestimoService.devolver(1L));

        // garante que NÃO incrementou nem gerou multa
        verify(livroRepository, never()).incrementarQuantidadeDisponivel(anyLong());
        verify(multaService, never()).gerarMultaSeAtrasado(any(), any());
    }

    @Test
    @DisplayName("devolver deve lançar EntidadeNaoEncontradaException quando empréstimo não existe")
    void devolver_lancaExcecaoQuandoEmprestimoNaoExiste() {
        when(emprestimoRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(EntidadeNaoEncontradaException.class,
                () -> emprestimoService.devolver(99L));
    }

    @Test
    @DisplayName("devolver deve incrementar quantidade e chamar MultaService quando válido")
    void devolver_incrementaEChamaMultaQuandoValido() {
        Emprestimo emp = emprestimo(1L);
        when(emprestimoRepository.buscarPorId(1L)).thenReturn(Optional.of(emp));

        Emprestimo resultado = emprestimoService.devolver(1L);

        assertNotNull(resultado);
        assertNotNull(resultado.getDataDevolucaoReal(), "dataDevolucaoReal deve ser preenchida");
        verify(livroRepository).incrementarQuantidadeDisponivel(emp.getLivroId());
        verify(emprestimoRepository).registrarDevolucao(anyLong(), any(LocalDateTime.class));
        verify(multaService).gerarMultaSeAtrasado(any(Emprestimo.class), any(LocalDateTime.class));
    }

    // ========== helpers ==========

    private Livro livro(Long id, String titulo, int quantidadeDisponivel) {
        return new Livro(id, titulo, "Autor Teste", "ISBN-" + id,
                quantidadeDisponivel + 1, quantidadeDisponivel, LocalDateTime.now());
    }

    private Usuario usuario(Long id, String nome) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(nome.toLowerCase() + "@test.com");
        u.setCpf("00000000000");
        u.setAtivo(true);
        return u;
    }

    private Emprestimo emprestimo(Long id) {
        Emprestimo emp = new Emprestimo();
        emp.setId(id);
        emp.setLivroId(1L);
        emp.setUsuarioId(1L);
        emp.setDataEmprestimo(LocalDateTime.of(2026, 1, 1, 10, 0));
        emp.setDataDevolucaoPrevista(LocalDateTime.of(2026, 1, 15, 10, 0));
        return emp;
    }
}
