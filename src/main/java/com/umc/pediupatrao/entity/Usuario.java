package com.umc.pediupatrao.entity;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Document(collection="usuarios")
public class Usuario {
    @Id private String id;
    private String username;
    @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) private String password;
    private String role;
    public String getId(){return id;} public void setId(String x){id=x;}
    public String getUsername(){return username;} public void setUsername(String x){username=x;}
    public String getPassword(){return password;} public void setPassword(String x){password=x;}
    public String getRole(){return role;} public void setRole(String x){role=x;}
}
