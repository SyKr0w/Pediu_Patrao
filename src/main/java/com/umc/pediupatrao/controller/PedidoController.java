package com.umc.pediupatrao.controller;
import com.umc.pediupatrao.dto.NovoPedidoRequest;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.PedidoService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;
    public PedidoController(PedidoService service){this.service=service;}
    @GetMapping public List<Pedido> listar(){return service.listarPedidos();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Pedido criar(@RequestBody NovoPedidoRequest r){return service.criarPedido(r);}
    @PutMapping("/{id}/status") public Pedido status(@PathVariable String id,@RequestParam String status){return service.atualizarStatus(id,status);}
    @PostMapping("/{id}/desconto") public Pedido desconto(@PathVariable String id,@RequestParam BigDecimal percentual){return service.aplicarDesconto(id,percentual);}
    @PostMapping("/{id}/cancelamento") public Pedido cancelar(@PathVariable String id,@RequestParam String justificativa){return service.cancelar(id,justificativa);}
}
