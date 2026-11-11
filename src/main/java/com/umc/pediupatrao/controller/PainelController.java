/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umc.pediupatrao.controller;

/**
 *
 * @author nocta
 */
import com.umc.pediupatrao.dto.PainelFiltro;
import com.umc.pediupatrao.dto.PainelResposta;
import com.umc.pediupatrao.service.PainelService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/painel")
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
public class PainelController {

    private final PainelService service;

    public PainelController(PainelService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    public PainelResposta.Resumo resumo(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String produtoId) {
        return service.resumo(new PainelFiltro(inicio, fim, status, produtoId));
    }

    @GetMapping("/historico")
    public List<PainelResposta.VendaDia> historico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String produtoId) {
        return service.historico(new PainelFiltro(inicio, fim, status, produtoId));
    }

    @GetMapping("/situacao")
    public List<PainelResposta.Situacao> situacao(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String produtoId) {
        return service.situacao(new PainelFiltro(inicio, fim, status, produtoId));
    }

    @GetMapping("/populares")
    public List<PainelResposta.Popular> populares(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String produtoId,
            @RequestParam(defaultValue = "5") int limite) {
        return service.populares(new PainelFiltro(inicio, fim, status, produtoId), limite);
    }

    @GetMapping("/produtos")
    public List<PainelResposta.ProdutoOpcao> produtos() {
        return service.listarProdutos();
    }
}
