package com.umc.pediupatrao.service;
import com.umc.pediupatrao.entity.*;
import com.umc.pediupatrao.repository.ClienteRepository;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
@Service
public class ClienteService {
    private final ClienteRepository repo;
    private final AuditoriaService auditoria;
    public ClienteService(ClienteRepository repo,AuditoriaService auditoria){this.repo=repo;this.auditoria=auditoria;}
    @PreAuthorize("hasAnyRole('GERENTE','ATENDENTE')")
    public List<Cliente> listarClientes(){return repo.findAll();}
    @PreAuthorize("hasAnyRole('GERENTE','ATENDENTE')")
    public Optional<Cliente> buscarPorId(String id){return repo.findById(id);}
    // Consulta limitada a nomes para a listagem de pedidos: ADMIN não recebe dados cadastrais completos.
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','ATENDENTE')")
    public Map<String,String> nomesParaConsultaPedidos(){
        Map<String,String> nomes=new HashMap<>();
        for(Cliente c:repo.findAll()){
            if(c.getId()!=null) nomes.put(c.getId(),c.getNome());
        }
        return nomes;
    }
    @PreAuthorize("hasRole('ATENDENTE')")
    public Cliente novoCliente(Cliente c){c.setId(null);return salvar(c);}
    @PreAuthorize("hasRole('ATENDENTE')")
    public Cliente salvar(Cliente recebido){
        if(recebido==null||recebido.getNome()==null||recebido.getNome().isBlank())throw new IllegalArgumentException("Nome do cliente é obrigatório.");
        Cliente antigo=null;
        if(recebido.getId()!=null&&!recebido.getId().isBlank()) antigo=repo.findById(recebido.getId()).orElseThrow(()->new IllegalArgumentException("Cliente não encontrado."));
        Cliente atual=antigo==null?new Cliente():antigo;
        Map<String,AlteracaoCampo> changes=new LinkedHashMap<>();
        // Explicitamente selecionar apenas campos cadastrais que a finalidade da auditoria requer.
        atualizar(changes,"nome",atual.getNome(),recebido.getNome());
        atualizar(changes,"telefone",atual.getTelefone(),recebido.getTelefone());
        atualizar(changes,"cep",atual.getCep(),recebido.getCep());
        atualizar(changes,"logradouro",atual.getLogradouro(),recebido.getLogradouro());
        atualizar(changes,"numero",atual.getNumero(),recebido.getNumero());
        atualizar(changes,"complemento",atual.getComplemento(),recebido.getComplemento());
        atualizar(changes,"bairro",atual.getBairro(),recebido.getBairro());
        atualizar(changes,"cidade",atual.getCidade(),recebido.getCidade());
        atualizar(changes,"estado",atual.getEstado(),recebido.getEstado());
        atualizar(changes,"endereco",atual.getEndereco(),recebido.getEndereco());
        if(changes.isEmpty()&&antigo!=null)return antigo;
        atual.setNome(recebido.getNome());atual.setTelefone(recebido.getTelefone());atual.setCep(recebido.getCep());
        atual.setLogradouro(recebido.getLogradouro());atual.setNumero(recebido.getNumero());atual.setComplemento(recebido.getComplemento());
        atual.setBairro(recebido.getBairro());atual.setCidade(recebido.getCidade());atual.setEstado(recebido.getEstado());atual.setEndereco(recebido.getEndereco());
        Cliente salvo=repo.save(atual);
        auditoria.registrar(antigo==null?"CRIACAO_CLIENTE":"ALTERACAO_CLIENTE","CLIENTE",salvo.getId(),changes,null);return salvo;
    }
    private void atualizar(Map<String,AlteracaoCampo> c,String campo,String anterior,String novo){AuditoriaService.alteracao(c,campo,anterior,novo);}
    @PreAuthorize("hasRole('GERENTE')")
    public void excluir(String id){
        Cliente c=repo.findById(id).orElseThrow(()->new IllegalArgumentException("Cliente não encontrado."));
        Map<String,AlteracaoCampo> campo=new LinkedHashMap<>();AuditoriaService.alteracao(campo,"nome",c.getNome(),null);
        AuditoriaService.alteracao(campo,"telefone",c.getTelefone(),null);
        AuditoriaService.alteracao(campo,"logradouro",c.getLogradouro(),null);
        repo.deleteById(id);auditoria.registrar("EXCLUSAO_CLIENTE","CLIENTE",id,campo,null);
    }
}
