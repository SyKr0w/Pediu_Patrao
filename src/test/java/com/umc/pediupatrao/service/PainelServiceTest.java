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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PainelServiceTest {

    @Mock PedidoRepository pedidos;
    @Mock ProdutoRepository produtos;
    PainelService service;
    LocalDate hoje;
    PainelFiltro filtro;

    @BeforeEach
    void iniciar() {
        service = new PainelService(pedidos, produtos);
        hoje = LocalDate.now();
        filtro = new PainelFiltro(hoje.minusDays(2), hoje, null, null);
    }

    private Pedido pedido(String status, BigDecimal total, LocalDateTime entrada,
                          LocalDateTime finalizacao, String produtoId, String nome, int quantidade) {
        Pedido p = new Pedido();
        p.setStatus(status);
        p.setTotal(total);
        p.setEntradaEm(entrada);
        p.setFinalizadoEm(finalizacao);
        ItemPedido item = new ItemPedido();
        item.setProdutoId(produtoId);
        item.setProdutoNome(nome);
        item.setQuantidade(quantidade);
        p.setItens(List.of(item));
        return p;
    }

    @Test
    void resumoConsideraSomentePedidosFinalizadosNoFaturamento() {
        Pedido finalizado = pedido("FINALIZADO", new BigDecimal("90.00"),
                hoje.atStartOfDay(), hoje.atTime(12, 0), "p1", "Pizza", 2);
        Pedido cancelado = pedido("CANCELADO", new BigDecimal("50.00"),
                hoje.atStartOfDay(), null, "p2", "Suco", 1);
        when(pedidos.findByFinalizadoEmGreaterThanEqualAndFinalizadoEmLessThan(any(), any()))
                .thenReturn(List.of(finalizado));
        when(pedidos.findByEntradaEmGreaterThanEqualAndEntradaEmLessThan(any(), any()))
                .thenReturn(List.of(finalizado, cancelado));

        PainelResposta.Resumo resposta = service.resumo(filtro);
        assertEquals(new BigDecimal("90.00"), resposta.faturamento());
        assertEquals(1, resposta.pedidosFinalizados());
        assertEquals(new BigDecimal("90.00"), resposta.ticketMedio());
        assertEquals(1, resposta.cancelamentos());
        assertEquals(2, resposta.pedidosRegistrados());
    }

    @Test
    void historicoUsaDataDeFinalizacaoEIncluiDiasSemVenda() {
        Pedido venda = pedido("FINALIZADO", new BigDecimal("25.00"),
                hoje.minusDays(2).atTime(10, 0), hoje.atTime(15, 0), "p1", "Pizza", 1);
        when(pedidos.findByFinalizadoEmGreaterThanEqualAndFinalizadoEmLessThan(any(), any()))
                .thenReturn(List.of(venda));

        List<PainelResposta.VendaDia> historico = service.historico(filtro);
        assertEquals(3, historico.size());
        assertEquals(BigDecimal.ZERO, historico.get(0).total());
        assertEquals(BigDecimal.ZERO, historico.get(1).total());
        assertEquals(new BigDecimal("25.00"), historico.get(2).total());
    }

    @Test
    void situacaoAgrupaPedidosPorStatus() {
        Pedido recebido = pedido("RECEBIDO", BigDecimal.TEN,
                hoje.atStartOfDay(), null, "p1", "Pizza", 1);
        Pedido cancelado = pedido("CANCELADO", BigDecimal.TEN,
                hoje.atStartOfDay(), null, "p1", "Pizza", 1);
        when(pedidos.findByEntradaEmGreaterThanEqualAndEntradaEmLessThan(any(), any()))
                .thenReturn(List.of(recebido, cancelado));

        List<PainelResposta.Situacao> resposta = service.situacao(filtro);
        assertEquals(1, resposta.stream().filter(s -> s.status().equals("RECEBIDO"))
                .findFirst().orElseThrow().quantidade());
        assertEquals(1, resposta.stream().filter(s -> s.status().equals("CANCELADO"))
                .findFirst().orElseThrow().quantidade());
    }

    @Test
    void popularesSomamQuantidadesEOrdenamEmColunas() {
        Pedido pedido1 = pedido("FINALIZADO", BigDecimal.TEN, hoje.atStartOfDay(),
                hoje.atTime(15, 0), "p1", "Pizza", 2);
        Pedido pedido2 = pedido("FINALIZADO", BigDecimal.TEN, hoje.atStartOfDay(),
                hoje.atTime(15, 0), "p1", "Pizza", 1);
        Pedido pedido3 = pedido("FINALIZADO", BigDecimal.TEN, hoje.atStartOfDay(),
                hoje.atTime(15, 0), "p2", "Suco", 2);
        when(pedidos.findByFinalizadoEmGreaterThanEqualAndFinalizadoEmLessThan(any(), any()))
                .thenReturn(List.of(pedido1, pedido2, pedido3));

        List<PainelResposta.Popular> populares = service.populares(filtro, 5);
        assertEquals(2, populares.size());
        assertEquals("Pizza", populares.get(0).nome());
        assertEquals(3, populares.get(0).quantidade());
        assertEquals(2, populares.get(1).quantidade());
    }

    @Test
    void filtraPorProdutoEStatusSemIncluirPedidosNaoFinalizadosNasVendas() {
        Pedido venda = pedido("FINALIZADO", new BigDecimal("50.00"),
                hoje.atStartOfDay(), hoje.atTime(18, 0), "p1", "Pizza", 1);
        when(pedidos.findByFinalizadoEmGreaterThanEqualAndFinalizadoEmLessThan(any(), any()))
                .thenReturn(List.of(venda));
        PainelFiltro porProduto = new PainelFiltro(hoje.minusDays(2), hoje, "FINALIZADO", "p2");
        assertEquals(BigDecimal.ZERO, service.historico(porProduto).get(2).total());
        assertEquals(0, service.populares(porProduto, 5).size());
        PainelFiltro naoFinalizado = new PainelFiltro(hoje.minusDays(2), hoje, "RECEBIDO", null);
        assertEquals(0, service.populares(naoFinalizado, 5).size());
    }

    @Test
    void rejeitaFiltrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> service.historico(
                new PainelFiltro(hoje, hoje.minusDays(1), null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.situacao(
                new PainelFiltro(hoje.minusDays(2), hoje, "INVENTADO", null)));
        assertThrows(IllegalArgumentException.class, () -> service.populares(filtro, 21));
        assertThrows(IllegalArgumentException.class, () -> service.historico(
                new PainelFiltro(hoje.minusDays(367), hoje, null, null)));
        verifyNoInteractions(pedidos);
    }

    @Test
    void bancoVazioRetornaIndicadoresZerados() {
        when(pedidos.findByFinalizadoEmGreaterThanEqualAndFinalizadoEmLessThan(any(), any()))
                .thenReturn(List.of());
        when(pedidos.findByEntradaEmGreaterThanEqualAndEntradaEmLessThan(any(), any()))
                .thenReturn(List.of());

        PainelResposta.Resumo resumo = service.resumo(filtro);
        assertEquals(BigDecimal.ZERO, resumo.faturamento());
        assertEquals(BigDecimal.ZERO, resumo.ticketMedio());
        assertEquals(0, resumo.pedidosFinalizados());
        assertEquals(0, resumo.cancelamentos());
    }

    @Test
    void listaProdutosParaFiltroMesmoQuandoInativos() {
        Produto p = new Produto();
        p.setId("p1");
        p.setNome("Pizza");
        p.setAtivo(false);
        when(produtos.findAll()).thenReturn(List.of(p));
        assertEquals("Pizza", service.listarProdutos().get(0).nome());
    }
}
