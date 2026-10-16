package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.repository.AuditoriaRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditoriaServiceTest {
    @AfterEach void limparContexto(){SecurityContextHolder.clearContext();}

    @Test void acessoNegadoRegistraUsuarioPerfilRotaE403() {
        AuditoriaRepository repo=mock(AuditoriaRepository.class);
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "atendente", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ATENDENTE"))));
        new AuditoriaService(repo).registrarAcessoNegado("/api/usuarios");
        ArgumentCaptor<Auditoria> captor=ArgumentCaptor.forClass(Auditoria.class);
        verify(repo).save(captor.capture());
        Auditoria evento=captor.getValue();
        assertEquals("ACESSO_NEGADO",evento.getOperacao());
        assertEquals("atendente",evento.getUsuario());
        assertEquals("ATENDENTE",evento.getPerfil());
        assertEquals("/api/usuarios",evento.getEntidadeId());
        assertFalse(evento.getCampos().containsKey("metodoHTTP"));
        assertEquals("403 - Acesso negado",evento.getCampos().get("resultado").getNovo());
        assertNotNull(evento.getDataHora());
    }
}
