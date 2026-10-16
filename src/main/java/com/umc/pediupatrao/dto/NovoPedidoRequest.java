package com.umc.pediupatrao.dto;
import java.util.List;
public class NovoPedidoRequest {
    private String clienteId;
    private List<ItemSolicitado> itens;
    public String getClienteId(){return clienteId;}public void setClienteId(String x){clienteId=x;}
    public List<ItemSolicitado> getItens(){return itens;}public void setItens(List<ItemSolicitado> x){itens=x;}
    public static class ItemSolicitado {
        private String produtoId; private int quantidade;
        public String getProdutoId(){return produtoId;}public void setProdutoId(String x){produtoId=x;}
        public int getQuantidade(){return quantidade;}public void setQuantidade(int x){quantidade=x;}
    }
}
