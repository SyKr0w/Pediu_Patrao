package com.umc.pediupatrao.entity;
import java.math.BigDecimal;
public class ItemPedido {
    private String produtoId;
    private String produtoNome;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subtotal;
    public String getProdutoId(){return produtoId;}public void setProdutoId(String x){produtoId=x;}
    public String getProdutoNome(){return produtoNome;}public void setProdutoNome(String x){produtoNome=x;}
    public int getQuantidade(){return quantidade;}public void setQuantidade(int x){quantidade=x;}
    public BigDecimal getPrecoUnitario(){return precoUnitario;}public void setPrecoUnitario(BigDecimal x){precoUnitario=x;}
    public BigDecimal getSubtotal(){return subtotal;}public void setSubtotal(BigDecimal x){subtotal=x;}
}
