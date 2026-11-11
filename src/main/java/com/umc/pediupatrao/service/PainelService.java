/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umc.pediupatrao.service;

/**
 *
 * @author nocta
 */
import com.umc.pediupatrao.dto.PainelFiltro;
import com.umc.pediupatrao.dto.PainelResposta;
import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class PainelService {

    private static final Map<String, String> STATUS = new LinkedHashMap<>();

    static {
        STATUS.put("RECEBIDO", "Recebido");
        STATUS.put("EM_PREPARACAO", "Em preparação");
        STATUS.put("PRONTO", "Pronto");
        STATUS.put("SAIU_PARA_ENTREGA", "Saiu para entrega");
        STATUS.put("RETIRADO", "Retirado");
        STATUS.put("FINALIZADO", "Finalizado");
        STATUS.put("CANCELADO", "Cancelado");
    }

    private final PedidoRepository pedidos;
    private final ProdutoRepository produtos;

    public PainelService(PedidoRepository pedidos, ProdutoRepository produtos) {
        this.pedidos = pedidos;
        this.produtos = produtos;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public PainelResposta.Resumo resumo(PainelFiltro filtro) {
        FiltroValidado f = validar(filtro);
        List<Pedido> finalizados = finalizarNoPeriodo(f);
        List<Pedido> entradas = entradasNoPeriodo(f);

        BigDecimal faturamento = finalizados.stream()
                .map(Pedido::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long cancelamentos = entradas.stream()
                .filter(p -> "CANCELADO".equals(p.getStatus())).count();
        BigDecimal ticket = finalizados.isEmpty() ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(finalizados.size()), 2, RoundingMode.HALF_UP);

        return new PainelResposta.Resumo(faturamento, finalizados.size(), ticket,
                cancelamentos, entradas.size());
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<PainelResposta.VendaDia> historico(PainelFiltro filtro) {
        FiltroValidado f = validar(filtro);
        Map<LocalDate, List<Pedido>> porDia = finalizarNoPeriodo(f).stream()
                .collect(Collectors.groupingBy(p -> p.getFinalizadoEm().toLocalDate()));
        List<PainelResposta.VendaDia> resultado = new ArrayList<>();

        for (LocalDate dia = f.inicio(); !dia.isAfter(f.fim()); dia = dia.plusDays(1)) {
            List<Pedido> pedidosDia = porDia.getOrDefault(dia, List.of());
            BigDecimal soma = pedidosDia.stream().map(Pedido::getTotal)
                    .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            resultado.add(new PainelResposta.VendaDia(dia, soma, pedidosDia.size()));
        }
        return resultado;
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<PainelResposta.Situacao> situacao(PainelFiltro filtro) {
        FiltroValidado f = validar(filtro);
        Map<String, Long> totais = entradasNoPeriodo(f).stream()
                .collect(Collectors.groupingBy(Pedido::getStatus, Collectors.counting()));
        return STATUS.entrySet().stream()
                .map(s -> new PainelResposta.Situacao(s.getKey(), s.getValue(),
                        totais.getOrDefault(s.getKey(), 0L)))
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<PainelResposta.Popular> populares(PainelFiltro filtro, int limite) {
        if (limite < 1 || limite > 20) {
            throw new IllegalArgumentException("O limite deve estar entre 1 e 20.");
        }
        FiltroValidado f = validar(filtro);
        Map<String, PopularAcumulado> acumulados = new LinkedHashMap<>();

        for (Pedido pedido : finalizarNoPeriodo(f)) {
            if (pedido.getItens() == null) {
                continue;
            }
            for (ItemPedido item : pedido.getItens()) {
                if (item == null || item.getProdutoId() == null || item.getQuantidade() <= 0) {
                    continue;
                }
                if (f.produtoId() != null && !f.produtoId().equals(item.getProdutoId())) {
                    continue;
                }
                PopularAcumulado a = acumulados.computeIfAbsent(item.getProdutoId(),
                        id -> new PopularAcumulado(id, item.getProdutoNome()));
                a.quantidade += item.getQuantidade();
            }
        }
        return acumulados.values().stream()
                .sorted(Comparator.comparingLong((PopularAcumulado a) -> a.quantidade).reversed()
                        .thenComparing(a -> a.nome))
                .limit(limite)
                .map(a -> new PainelResposta.Popular(a.id, a.nome, a.quantidade))
                .toList();
    }

    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<PainelResposta.ProdutoOpcao> listarProdutos() {
        return produtos.findAll().stream()
                .filter(p -> p.getId() != null && p.getNome() != null)
                .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(p -> new PainelResposta.ProdutoOpcao(p.getId(), p.getNome()))
                .toList();
    }

    private List<Pedido> finalizarNoPeriodo(FiltroValidado f) {
        if (f.status() != null && !"FINALIZADO".equals(f.status())) {
            return List.of();
        }
        return pedidos.findByFinalizadoEmGreaterThanEqualAndFinalizadoEmLessThan(
                f.inicio().atStartOfDay(), f.fim().plusDays(1).atStartOfDay()).stream()
                .filter(p -> "FINALIZADO".equals(p.getStatus()))
                .filter(p -> contemProduto(p, f.produtoId()))
                .toList();
    }

    private List<Pedido> entradasNoPeriodo(FiltroValidado f) {
        return pedidos.findByEntradaEmGreaterThanEqualAndEntradaEmLessThan(
                f.inicio().atStartOfDay(), f.fim().plusDays(1).atStartOfDay()).stream()
                .filter(p -> f.status() == null || f.status().equals(p.getStatus()))
                .filter(p -> contemProduto(p, f.produtoId()))
                .toList();
    }

    private boolean contemProduto(Pedido pedido, String produtoId) {
        if (produtoId == null) {
            return true;
        }
        return pedido.getItens() != null && pedido.getItens().stream()
                .anyMatch(i -> i != null && produtoId.equals(i.getProdutoId()));
    }

    private FiltroValidado validar(PainelFiltro filtro) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = filtro == null || filtro.inicio() == null ? hoje.minusDays(29) : filtro.inicio();
        LocalDate fim = filtro == null || filtro.fim() == null ? hoje : filtro.fim();
        if (inicio.isAfter(fim)) {
            throw new IllegalArgumentException("A data inicial deve ser anterior ou igual à data final.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) > 365) {
            throw new IllegalArgumentException("O período máximo é de 366 dias.");
        }
        String status = normalizar(filtro == null ? null : filtro.status());
        if (status != null && !STATUS.containsKey(status)) {
            throw new IllegalArgumentException("Status inválido.");
        }
        String produtoId = normalizar(filtro == null ? null : filtro.produtoId());
        return new FiltroValidado(inicio, fim, status, produtoId);
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private record FiltroValidado(LocalDate inicio, LocalDate fim, String status, String produtoId) {
    }

    private static class PopularAcumulado {
        final String id;
        final String nome;
        long quantidade;

        PopularAcumulado(String id, String nome) {
            this.id = id;
            this.nome = nome == null || nome.isBlank() ? "Produto sem nome" : nome;
        }
    }
}
