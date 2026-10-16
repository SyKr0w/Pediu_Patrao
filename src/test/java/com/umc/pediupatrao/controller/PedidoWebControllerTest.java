package com.umc.pediupatrao.controller;

import com.umc.pediupatrao.service.AuditoriaService;
import com.umc.pediupatrao.service.ClienteService;
import com.umc.pediupatrao.service.PedidoService;
import com.umc.pediupatrao.service.ProdutoService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoWebControllerTest {
    @Test void listagemUsaConsultaDeNomesPermitidaParaAdmin() {
        PedidoService pedidos=mock(PedidoService.class);
        ClienteService clientes=mock(ClienteService.class);
        ProdutoService produtos=mock(ProdutoService.class);
        AuditoriaService auditoria=mock(AuditoriaService.class);
        when(pedidos.listarPedidos()).thenReturn(List.of());
        when(clientes.nomesParaConsultaPedidos()).thenReturn(Map.of("c1","Maria"));
        PedidoWebController tela=new PedidoWebController(pedidos,clientes,produtos,auditoria);
        ExtendedModelMap modelo=new ExtendedModelMap();
        assertEquals("layout",tela.lista(modelo));
        assertEquals("pedidos :: content",modelo.get("content"));
        assertEquals(Map.of("c1","Maria"),modelo.get("nomesClientes"));
        verify(clientes,never()).listarClientes();
    }
}
