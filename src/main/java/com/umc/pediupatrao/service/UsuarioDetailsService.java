package com.umc.pediupatrao.service;
import com.umc.pediupatrao.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repository;
    public UsuarioDetailsService(UsuarioRepository repository) {this.repository = repository;}
    @Override public UserDetails loadUserByUsername(String name) {
        return repository.findByUsername(name).map(u -> User.withUsername(u.getUsername())
            .password(u.getPassword()).roles("USER".equals(u.getRole()) ? "ATENDENTE" : u.getRole() == null ? "ATENDENTE" : u.getRole())
            .build()).orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }
}
