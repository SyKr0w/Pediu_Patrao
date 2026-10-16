package com.umc.pediupatrao.service;
import com.umc.pediupatrao.entity.*;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service
public class AuditoriaService {
    private final AuditoriaRepository repository;
    public AuditoriaService(AuditoriaRepository repo){this.repository=repo;}
    // Não registra senhas ou tokens: somente campos de negócio escolhidos pelo serviço chamador.
    public void registrar(String operacao,String entidade,String id,Map<String,AlteracaoCampo> campos,String justificativa) {
        Authentication auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null||!auth.isAuthenticated()) throw new IllegalStateException("Auditoria requer usuário autenticado.");
        Auditoria evento=new Auditoria();evento.setUsuario(auth.getName());
        evento.setPerfil(auth.getAuthorities().stream().map(a->a.getAuthority()).filter(a->a.startsWith("ROLE_")).findFirst().orElse("SEM_PERFIL").replace("ROLE_",""));
        evento.setDataHora(LocalDateTime.now());evento.setOperacao(operacao);evento.setEntidade(entidade);
        evento.setEntidadeId(id);evento.setCampos(new LinkedHashMap<>(campos));evento.setJustificativa(justificativa);
        repository.save(evento);
    }
    // Evento técnico de segurança. Nunca armazena parâmetros, cabeçalhos ou corpo da requisição.
    public void registrarAcessoNegado(String caminho){
        Map<String,AlteracaoCampo> campos=new LinkedHashMap<>();
        campos.put("resultado",new AlteracaoCampo(null,"403 - Acesso negado"));
        registrar("ACESSO_NEGADO","ROTA",caminho,campos,null);
    }
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public List<Auditoria> listar(){return repository.findAllByOrderByDataHoraDesc();}
    public static void alteracao(Map<String,AlteracaoCampo> mapa,String campo,Object antigo,Object novo){
        if(!Objects.equals(antigo,novo)) mapa.put(campo,new AlteracaoCampo(antigo,novo));
    }
}
