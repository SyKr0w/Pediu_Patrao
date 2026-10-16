package com.umc.pediupatrao.service;
import com.umc.pediupatrao.dto.NovoPedidoRequest;
import com.umc.pediupatrao.entity.*;
import com.umc.pediupatrao.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;
    private final AuditoriaService auditoria;
    public PedidoService(PedidoRepository pedidos,ClienteRepository clientes,ProdutoRepository produtos,AuditoriaService auditoria){
        this.pedidos=pedidos;this.clientes=clientes;this.produtos=produtos;this.auditoria=auditoria;
    }
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','ATENDENTE')")
    public List<Pedido> listarPedidos(){return pedidos.findAll();}
    @PreAuthorize("hasAnyRole('GERENTE','ATENDENTE')")
    public Pedido criarPedido(NovoPedidoRequest req) {
        if(req==null||req.getClienteId()==null||!clientes.existsById(req.getClienteId())) throw new IllegalArgumentException("Selecione um cliente cadastrado.");
        if(req.getItens()==null||req.getItens().isEmpty()) throw new IllegalArgumentException("Informe pelo menos um item.");
        Pedido p=new Pedido();p.setClienteId(req.getClienteId());p.setStatus("RECEBIDO");
        p.setEntradaEm(LocalDateTime.now());p.setEntradaPor(usuarioAtual());
        BigDecimal soma=BigDecimal.ZERO;
        for(NovoPedidoRequest.ItemSolicitado solicitado:req.getItens()) {
            if(solicitado==null||solicitado.getQuantidade()<1||solicitado.getQuantidade()>100) throw new IllegalArgumentException("Quantidade inválida (1 a 100).");
            Produto produto=produtos.findById(solicitado.getProdutoId()).orElseThrow(()->new IllegalArgumentException("Produto inexistente."));
            if(!Boolean.TRUE.equals(produto.getAtivo())||produto.getPreco()==null||!Double.isFinite(produto.getPreco())||produto.getPreco()<0) throw new IllegalArgumentException("Produto indisponível.");
            ItemPedido item=new ItemPedido();item.setProdutoId(produto.getId());item.setProdutoNome(produto.getNome());item.setQuantidade(solicitado.getQuantidade());
            item.setPrecoUnitario(BigDecimal.valueOf(produto.getPreco()).setScale(2,RoundingMode.HALF_UP));
            item.setSubtotal(item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())));
            p.getItens().add(item);soma=soma.add(item.getSubtotal());
        }
        p.setSubtotal(soma);p.setTotal(soma);p.setQuantidade(p.getItens().stream().mapToInt(ItemPedido::getQuantidade).sum());
        Pedido salvo=pedidos.save(p);
        Map<String,AlteracaoCampo> campos=new LinkedHashMap<>();
        AuditoriaService.alteracao(campos,"status",null,salvo.getStatus());
        AuditoriaService.alteracao(campos,"clienteId",null,salvo.getClienteId());
        AuditoriaService.alteracao(campos,"itens",null,descricaoItens(salvo));
        AuditoriaService.alteracao(campos,"total",null,salvo.getTotal());
        AuditoriaService.alteracao(campos,"entradaEm",null,salvo.getEntradaEm());
        AuditoriaService.alteracao(campos,"entradaPor",null,salvo.getEntradaPor());
        auditoria.registrar("CRIACAO_PEDIDO","PEDIDO",salvo.getId(),campos,null);
        return salvo;
    }
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido atualizarStatus(String id,String status) {
        Pedido p=buscar(id);String anterior=p.getStatus();
        if(p.getEntradaEm()==null) throw new IllegalArgumentException("Pedido antigo sem entrada registrada. Cadastre um novo pedido.");
        if(!proximosStatus(anterior).contains(status)) throw new IllegalArgumentException("Transição inválida: "+anterior+" -> "+status);
        p.setStatus(status);
        Map<String,AlteracaoCampo> changes=new LinkedHashMap<>();AuditoriaService.alteracao(changes,"status",anterior,status);
        if("SAIU_PARA_ENTREGA".equals(status)||"RETIRADO".equals(status)) {
            p.setSaidaEm(LocalDateTime.now());p.setSaidaPor(usuarioAtual());
            AuditoriaService.alteracao(changes,"saidaEm",null,p.getSaidaEm());
            AuditoriaService.alteracao(changes,"saidaPor",null,p.getSaidaPor());
        }
        Pedido result=pedidos.save(p);auditoria.registrar("ALTERACAO_STATUS","PEDIDO",id,changes,null);return result;
    }
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido aplicarDesconto(String id,BigDecimal percentual) {
        Pedido p=buscar(id);
        if(!List.of("RECEBIDO","EM_PREPARACAO","PRONTO").contains(p.getStatus())) throw new IllegalArgumentException("Desconto só é permitido antes da saída.");
        if(percentual==null||percentual.compareTo(BigDecimal.ZERO)<0||percentual.compareTo(new BigDecimal("20"))>0) throw new IllegalArgumentException("Desconto deve estar entre 0 e 20%.");
        if(p.getSubtotal()==null||p.getItens()==null||p.getItens().isEmpty()) throw new IllegalArgumentException("Pedido antigo sem itens e preços; cadastre um novo pedido.");
        Map<String,AlteracaoCampo> changes=new LinkedHashMap<>();
        BigDecimal desconto=p.getSubtotal().multiply(percentual).divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);
        BigDecimal total=p.getSubtotal().subtract(desconto);
        AuditoriaService.alteracao(changes,"percentualDesconto",p.getPercentualDesconto(),percentual);
        AuditoriaService.alteracao(changes,"valorDesconto",p.getValorDesconto(),desconto);
        AuditoriaService.alteracao(changes,"total",p.getTotal(),total);
        if(changes.isEmpty()) throw new IllegalArgumentException("O desconto informado já está aplicado.");
        p.setPercentualDesconto(percentual);p.setValorDesconto(desconto);p.setTotal(total);
        Pedido salvo=pedidos.save(p);auditoria.registrar("DESCONTO","PEDIDO",id,changes,null);return salvo;
    }
    @PreAuthorize("hasRole('GERENTE')")
    public Pedido cancelar(String id,String justificativa) {
        Pedido p=buscar(id);
        if(!List.of("RECEBIDO","EM_PREPARACAO","PRONTO").contains(p.getStatus())) throw new IllegalArgumentException("Não é possível cancelar após a saída ou finalização.");
        if(justificativa==null||justificativa.isBlank()) throw new IllegalArgumentException("A justificativa é obrigatória.");
        String motivo=justificativa.trim();if(motivo.length()>500) throw new IllegalArgumentException("Justificativa deve ter até 500 caracteres.");
        String anterior=p.getStatus();p.setStatus("CANCELADO");p.setJustificativaCancelamento(motivo);
        Pedido salvo=pedidos.save(p);Map<String,AlteracaoCampo> changes=new LinkedHashMap<>();
        AuditoriaService.alteracao(changes,"status",anterior,"CANCELADO");
        AuditoriaService.alteracao(changes,"justificativaCancelamento",null,motivo);
        auditoria.registrar("CANCELAMENTO","PEDIDO",id,changes,motivo);return salvo;
    }
    public static List<String> proximosStatus(String s){
        if("RECEBIDO".equals(s))return List.of("EM_PREPARACAO");
        if("EM_PREPARACAO".equals(s))return List.of("PRONTO");
        if("PRONTO".equals(s))return List.of("SAIU_PARA_ENTREGA","RETIRADO");
        if("SAIU_PARA_ENTREGA".equals(s)||"RETIRADO".equals(s))return List.of("FINALIZADO");
        return List.of();
    }
    private Pedido buscar(String id){return pedidos.findById(id).orElseThrow(()->new IllegalArgumentException("Pedido não encontrado."));}
    private String usuarioAtual(){return SecurityContextHolder.getContext().getAuthentication().getName();}
    private String descricaoItens(Pedido p){return p.getItens().stream().map(i->i.getProdutoNome()+" x"+i.getQuantidade()+" @ "+i.getPrecoUnitario()).reduce((a,b)->a+"; "+b).orElse("");}
}
