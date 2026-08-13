package com.davi.biblioteca.jpa.controller;

import com.davi.biblioteca.jpa.entity.MultaEntity;
import com.davi.biblioteca.jpa.service.MultaJpaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller demonstrativo JPA para multas. Convivência lado a lado com o
 * controller JDBC em /multas.
 */
@RestController
@RequestMapping("/api/jpa/multas")
@Tag(name = "Multa (JPA)", description = "Endpoints demonstrativos via Spring Data JPA")
public class MultaJpaController {

    private final MultaJpaService service;

    public MultaJpaController(MultaJpaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista multas via JPA (?emAberto=true filtra não pagas)")
    public ResponseEntity<List<MultaEntity>> listar(
            @RequestParam(name = "emAberto", defaultValue = "false") boolean emAberto) {
        return ResponseEntity.ok(emAberto ? service.listarEmAberto() : service.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma multa por ID via JPA")
    public ResponseEntity<MultaEntity> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping("/{id}/pagar")
    @Operation(summary = "Marca uma multa como paga via JPA")
    public ResponseEntity<MultaEntity> pagar(@PathVariable Long id) {
        return ResponseEntity.ok(service.pagar(id));
    }
}
