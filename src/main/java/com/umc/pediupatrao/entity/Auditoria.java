package com.umc.pediupatrao.entity;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection="auditoria")
public class Auditoria {
    @Id private String id;
    private String usuario;
    private String perfil;
    private LocalDateTime dataHora;
    private String operacao;
    private String entidade;
    private String entidadeId;
    private Map<String, AlteracaoCampo> campos = new LinkedHashMap<>();
    private String justificativa;
    public String getId(){return id;}public void setId(String x){id=x;}
    public String getUsuario(){return usuario;}public void setUsuario(String x){usuario=x;}
    public String getPerfil(){return perfil;}public void setPerfil(String x){perfil=x;}
    public LocalDateTime getDataHora(){return dataHora;}public void setDataHora(LocalDateTime x){dataHora=x;}
    public String getOperacao(){return operacao;}public void setOperacao(String x){operacao=x;}
    public String getEntidade(){return entidade;}public void setEntidade(String x){entidade=x;}
    public String getEntidadeId(){return entidadeId;}public void setEntidadeId(String x){entidadeId=x;}
    public Map<String,AlteracaoCampo> getCampos(){return campos;} public void setCampos(Map<String,AlteracaoCampo> x){campos=x;}
    public String getJustificativa(){return justificativa;}public void setJustificativa(String x){justificativa=x;}
}
