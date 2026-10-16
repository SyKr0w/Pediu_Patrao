package com.umc.pediupatrao.service;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioService {
    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder){this.repo=repo;this.encoder=encoder;}
    public Optional<Usuario> buscarPorId(String id){return repo.findById(id);}
    public Optional<Usuario> buscarPorUsername(String nome){return repo.findByUsername(nome);}
    public List<Usuario> listarTodos(){return repo.findAll();}
    public Usuario salvarUsuario(Usuario u) {
        if(u.getId()!=null && !u.getId().isBlank()) throw new IllegalArgumentException("Use a operação de atualização.");
        String nome=validarNome(u.getUsername()); validarPerfil(u.getRole());
        if(repo.findByUsername(nome).isPresent()) throw new IllegalArgumentException("Nome de usuário já utilizado.");
        if(u.getPassword()==null || u.getPassword().length()<6) throw new IllegalArgumentException("A senha deve ter ao menos 6 caracteres.");
        Usuario novo = new Usuario();novo.setUsername(nome);novo.setRole(u.getRole());novo.setPassword(encoder.encode(u.getPassword()));
        return repo.save(novo);
    }
    public Usuario atualizarUsuario(String id, Usuario u) {
        Usuario atual=repo.findById(id).orElseThrow(()->new IllegalArgumentException("Usuário não encontrado."));
        String nome=validarNome(u.getUsername());validarPerfil(u.getRole());
        repo.findByUsername(nome).ifPresent(existente -> {
            if(!existente.getId().equals(id)) throw new IllegalArgumentException("Nome de usuário já utilizado.");
        });
        if("ADMIN".equals(atual.getRole()) && !"ADMIN".equals(u.getRole()) && repo.findAll().stream().filter(a->"ADMIN".equals(a.getRole())).count()<=1)
            throw new IllegalArgumentException("O sistema precisa manter ao menos um administrador.");
        Authentication a=SecurityContextHolder.getContext().getAuthentication();
        if(a!=null && a.getName().equals(atual.getUsername()) && !"ADMIN".equals(u.getRole()))
            throw new IllegalArgumentException("Não é permitido remover o próprio perfil ADMIN.");
        atual.setUsername(nome);atual.setRole(u.getRole());
        if(u.getPassword()!=null && !u.getPassword().isBlank()) {
            if(u.getPassword().length()<6) throw new IllegalArgumentException("A senha deve ter ao menos 6 caracteres.");
            atual.setPassword(encoder.encode(u.getPassword()));
        }
        return repo.save(atual);
    }
    public void deletarUsuario(String id) {
        Usuario usuario=repo.findById(id).orElseThrow(()->new IllegalArgumentException("Usuário não encontrado."));
        Authentication a=SecurityContextHolder.getContext().getAuthentication();
        if(a!=null && a.getName().equals(usuario.getUsername())) throw new IllegalArgumentException("Não é permitido excluir a própria conta.");
        if("ADMIN".equals(usuario.getRole()) && repo.findAll().stream().filter(u->"ADMIN".equals(u.getRole())).count()<=1)
            throw new IllegalArgumentException("Não é permitido excluir o último administrador.");
        repo.delete(usuario);
    }
    private String validarNome(String n){if(n==null||n.isBlank()) throw new IllegalArgumentException("Informe o usuário.");return n.trim();}
    private void validarPerfil(String r){if(!List.of("ADMIN","GERENTE","ATENDENTE").contains(r)) throw new IllegalArgumentException("Perfil inválido.");}
}
