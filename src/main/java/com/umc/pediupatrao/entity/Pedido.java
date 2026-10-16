package com.umc.pediupatrao.entity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection="pedidos")
public class Pedido {
    @Id private String id;
    private String clienteId;
    // Campos legados mantidos para leitura de pedidos anteriores.
    private String tipoPizza;
    private String sabor;
    private int quantidade;
    private String status;
    private List<ItemPedido> itens=new ArrayList<>();
    private BigDecimal subtotal=BigDecimal.ZERO;
    private BigDecimal percentualDesconto=BigDecimal.ZERO;
    private BigDecimal valorDesconto=BigDecimal.ZERO;
    private BigDecimal total=BigDecimal.ZERO;
    private LocalDateTime entradaEm;
    private String entradaPor;
    private LocalDateTime saidaEm;
    private String saidaPor;
    private String justificativaCancelamento;
    public String getId(){return id;} public void setId(String x){id=x;}
    public String getClienteId(){return clienteId;} public void setClienteId(String x){clienteId=x;}
    public String getTipoPizza(){return tipoPizza;} public void setTipoPizza(String x){tipoPizza=x;}
    public String getSabor(){return sabor;} public void setSabor(String x){sabor=x;}
    public int getQuantidade(){return quantidade;}public void setQuantidade(int x){quantidade=x;}
    public String getStatus(){return status;}public void setStatus(String x){status=x;}
    public List<ItemPedido> getItens(){return itens;}public void setItens(List<ItemPedido> x){itens=x;}
    public BigDecimal getSubtotal(){return subtotal;}public void setSubtotal(BigDecimal x){subtotal=x;}
    public BigDecimal getPercentualDesconto(){return percentualDesconto;}public void setPercentualDesconto(BigDecimal x){percentualDesconto=x;}
    public BigDecimal getValorDesconto(){return valorDesconto;}public void setValorDesconto(BigDecimal x){valorDesconto=x;}
    public BigDecimal getTotal(){return total;}public void setTotal(BigDecimal x){total=x;}
    public LocalDateTime getEntradaEm(){return entradaEm;}public void setEntradaEm(LocalDateTime x){entradaEm=x;}
    public String getEntradaPor(){return entradaPor;}public void setEntradaPor(String x){entradaPor=x;}
    public LocalDateTime getSaidaEm(){return saidaEm;}public void setSaidaEm(LocalDateTime x){saidaEm=x;}
    public String getSaidaPor(){return saidaPor;}public void setSaidaPor(String x){saidaPor=x;}
    public String getJustificativaCancelamento(){return justificativaCancelamento;}public void setJustificativaCancelamento(String x){justificativaCancelamento=x;}
}
