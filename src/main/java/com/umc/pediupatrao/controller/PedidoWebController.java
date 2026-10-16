package com.umc.pediupatrao.controller;
import com.umc.pediupatrao.dto.NovoPedidoRequest;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.service.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.ItemPedido;

@Controller
public class PedidoWebController {
    private final PedidoService pedidos;
    private final ClienteService clientes;
    private final ProdutoService produtos;
    private final AuditoriaService auditoria;
    public PedidoWebController(PedidoService p,ClienteService c,ProdutoService pr,AuditoriaService a){pedidos=p;clientes=c;produtos=pr;auditoria=a;}

    @GetMapping("/pedidos")
    public String lista(Model m) {
        List<Pedido> todos = pedidos.listarPedidos();
        m.addAttribute("pedidos", todos);

        Map<String, List<String>> passos = new HashMap<>();
        for (Pedido p : todos) {
            passos.put(p.getId(), PedidoService.proximosStatus(p.getStatus()));
        }
        m.addAttribute("proximos", passos);

        m.addAttribute("nomesClientes", clientes.nomesParaConsultaPedidos());

        m.addAttribute("content", "pedidos :: content");
        return "layout";
    }

    @GetMapping("/pedidos/novo")
    public String novo(Model m) {
        m.addAttribute("clientes", clientes.listarClientes());
        m.addAttribute("produtos", produtos.listarProdutos());
        m.addAttribute("content", "pedidos/novo :: content");
        return "layout";
    }

    @PostMapping("/pedidos/novo") public String criar(@RequestParam String clienteId,@RequestParam List<String> produtoId,
            @RequestParam List<Integer> quantidade,RedirectAttributes r){
        try {
            if(produtoId.size()!=quantidade.size()) throw new IllegalArgumentException("Informe produto e quantidade para cada item.");
            NovoPedidoRequest req=new NovoPedidoRequest();req.setClienteId(clienteId);
            List<NovoPedidoRequest.ItemSolicitado> itens=new ArrayList<>();
            for(int i=0;i<produtoId.size();i++) {
                NovoPedidoRequest.ItemSolicitado item=new NovoPedidoRequest.ItemSolicitado();item.setProdutoId(produtoId.get(i));item.setQuantidade(quantidade.get(i));itens.add(item);
            }
            req.setItens(itens);pedidos.criarPedido(req);r.addFlashAttribute("sucesso","Pedido registrado.");
        }catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/pedidos";
    }
    @PostMapping("/pedidos/{id}/status") public String status(@PathVariable String id,@RequestParam String status,RedirectAttributes r){
        try{pedidos.atualizarStatus(id,status);r.addFlashAttribute("sucesso","Status atualizado.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/pedidos";
    }
    @PostMapping("/pedidos/{id}/desconto") public String desconto(@PathVariable String id,@RequestParam BigDecimal percentual,RedirectAttributes r){
        try{pedidos.aplicarDesconto(id,percentual);r.addFlashAttribute("sucesso","Desconto aplicado.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/pedidos";
    }
    @PostMapping("/pedidos/{id}/cancelar") public String cancelar(@PathVariable String id,@RequestParam String justificativa,RedirectAttributes r){
        try{pedidos.cancelar(id,justificativa);r.addFlashAttribute("sucesso","Pedido cancelado.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/pedidos";
    }
    @GetMapping("/auditoria")
    public String auditoria(Model m){
        List<Auditoria> registros = auditoria.listar();
        Map<String,String> nomesClientes = clientes.nomesParaConsultaPedidos();

        // Recupera um nome histórico quando o cliente já foi excluído.
        for(Auditoria registro : registros){
            if(!"CLIENTE".equals(registro.getEntidade()) || nomesClientes.containsKey(registro.getEntidadeId())) continue;
            Map<String,AlteracaoCampo> campos = registro.getCampos();
            AlteracaoCampo nome = campos == null ? null : campos.get("nome");
            if(nome != null){
                String legivel = nome.getNovo() == null ? nome.getAnterior() : nome.getNovo();
                if(legivel != null && !legivel.isBlank()) nomesClientes.put(registro.getEntidadeId(),legivel);
            }
        }

        Map<String,String> descricoes = new HashMap<>();
        for(Map.Entry<String,String> cliente : nomesClientes.entrySet()){
            descricoes.put("CLIENTE:"+cliente.getKey(),"Cliente: "+cliente.getValue());
        }
        for(Pedido pedido : pedidos.listarPedidos()){
            String nomeCliente = nomesClientes.getOrDefault(pedido.getClienteId(),"Cliente não localizado");
            String itens = resumoItens(pedido);
            descricoes.put("PEDIDO:"+pedido.getId(),"Pedido de "+nomeCliente+(itens.isBlank()?"":" — "+itens));
        }

        m.addAttribute("registros",registros);
        m.addAttribute("nomesClientes",nomesClientes);
        m.addAttribute("descricoesEntidades",descricoes);
        m.addAttribute("content","auditoria :: content");
        return "layout";
    }

    private String resumoItens(Pedido pedido){
        if(pedido.getItens()!=null && !pedido.getItens().isEmpty()){
            List<String> partes = new ArrayList<>();
            for(ItemPedido item : pedido.getItens()){
                if(item != null) partes.add(item.getProdutoNome()+" x"+item.getQuantidade());
                if(partes.size()==2) break;
            }
            return String.join(", ",partes)+(pedido.getItens().size()>2?"...":"");
        }
        return pedido.getSabor()==null?"":pedido.getSabor()+" x"+pedido.getQuantidade();
    }
}
