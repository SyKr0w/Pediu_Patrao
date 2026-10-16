package com.umc.pediupatrao.service;

import com.umc.pediupatrao.dto.NovoPedidoRequest;
import com.umc.pediupatrao.entity.*;
import com.umc.pediupatrao.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {
    @Mock PedidoRepository pedidos;
    @Mock ClienteRepository clientes;
    @Mock ProdutoRepository produtos;
    @Mock AuditoriaService auditoria;
    PedidoService service;

    @BeforeEach void iniciar() {
        service=new PedidoService(pedidos,clientes,produtos,auditoria);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("atendente01", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ATENDENTE"))));
    }
    @org.junit.jupiter.api.AfterEach void limpar(){SecurityContextHolder.clearContext();}

    @Test void totalCalculadoNoServidorEEntradaRegistrada() {
        when(clientes.existsById("cliente1")).thenReturn(true);
        Produto produto=new Produto();produto.setId("p1");produto.setNome("Calabresa");produto.setPreco(30.50);produto.setAtivo(true);
        when(produtos.findById("p1")).thenReturn(Optional.of(produto));
        when(pedidos.save(any(Pedido.class))).thenAnswer(invocation->{Pedido p=invocation.getArgument(0);p.setId("id1");return p;});
        NovoPedidoRequest.ItemSolicitado item=new NovoPedidoRequest.ItemSolicitado();item.setProdutoId("p1");item.setQuantidade(2);
        NovoPedidoRequest req=new NovoPedidoRequest();req.setClienteId("cliente1");req.setItens(List.of(item));
        Pedido criado=service.criarPedido(req);
        assertEquals(new BigDecimal("61.00"),criado.getTotal());
        assertEquals("RECEBIDO",criado.getStatus());
        assertNotNull(criado.getEntradaEm());assertEquals("atendente01",criado.getEntradaPor());
        verify(auditoria).registrar(eq("CRIACAO_PEDIDO"),eq("PEDIDO"),eq("id1"),anyMap(),isNull());
    }
    @Test void pedidoNaoPodePularEtapas() {
        Pedido pedido=new Pedido();pedido.setStatus("RECEBIDO");pedido.setEntradaEm(java.time.LocalDateTime.now());
        when(pedidos.findById("id1")).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class,()->service.atualizarStatus("id1","FINALIZADO"));
        verify(pedidos,never()).save(any());
    }
    @Test void pedidoCanceladoNaoPodeAvancar() {
        Pedido pedido=new Pedido();pedido.setStatus("CANCELADO");pedido.setEntradaEm(java.time.LocalDateTime.now());
        when(pedidos.findById("id1")).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class,()->service.atualizarStatus("id1","PRONTO"));
    }
    @Test void percentualMaiorQueVinteEhRejeitado() {
        Pedido pedido=new Pedido();pedido.setStatus("RECEBIDO");pedido.setSubtotal(new BigDecimal("100.00"));
        when(pedidos.findById("id1")).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class,()->service.aplicarDesconto("id1",new BigDecimal("21")));
        verify(pedidos,never()).save(any());
    }
    @Test void naoCancelaSemJustificativa() {
        Pedido pedido=new Pedido();pedido.setStatus("PRONTO");
        when(pedidos.findById("id1")).thenReturn(Optional.of(pedido));
        assertThrows(IllegalArgumentException.class,()->service.cancelar("id1"," "));
    }
    @Test void saidaRegistraHoraEUsuario() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("gerente01", "N/A", List.of(new SimpleGrantedAuthority("ROLE_GERENTE"))));
        Pedido pedido=new Pedido();pedido.setStatus("PRONTO");pedido.setEntradaEm(java.time.LocalDateTime.now());
        when(pedidos.findById("id1")).thenReturn(Optional.of(pedido));
        when(pedidos.save(any())).thenAnswer(invocation->invocation.getArgument(0));
        Pedido alterado=service.atualizarStatus("id1","RETIRADO");
        assertNotNull(alterado.getSaidaEm());assertEquals("gerente01",alterado.getSaidaPor());
        verify(auditoria).registrar(eq("ALTERACAO_STATUS"),eq("PEDIDO"),eq("id1"),anyMap(),isNull());
    }
}
