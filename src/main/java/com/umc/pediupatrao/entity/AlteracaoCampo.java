package com.umc.pediupatrao.entity;
public class AlteracaoCampo {
    private String anterior;
    private String novo;
    public AlteracaoCampo(){}
    public AlteracaoCampo(Object anterior,Object novo){this.anterior=anterior==null?null:String.valueOf(anterior);this.novo=novo==null?null:String.valueOf(novo);}
    public String getAnterior(){return anterior;} public void setAnterior(String x){anterior=x;}
    public String getNovo(){return novo;} public void setNovo(String x){novo=x;}
}
