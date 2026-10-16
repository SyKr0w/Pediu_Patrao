package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.Mockito.*;

class AcessoNegadoHandlerTest {
    @AfterEach void limparContexto(){SecurityContextHolder.clearContext();}

    @Test void registraTentativaEContinuaNegandoARequisicao() throws Exception {
        AuditoriaService auditoria=mock(AuditoriaService.class);
        HttpServletRequest req=mock(HttpServletRequest.class);
        HttpServletResponse resp=mock(HttpServletResponse.class);
        when(req.getRequestURI()).thenReturn("/api/pedidos/abc/desconto");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        new AcessoNegadoHandler(auditoria).handle(req,resp,new AccessDeniedException("Proibido"));
        verify(auditoria).registrarAcessoNegado("/api/pedidos/abc/desconto");
        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test void falhaNaAuditoriaNaoRemoveBloqueio403() throws Exception {
        AuditoriaService auditoria=mock(AuditoriaService.class);
        HttpServletRequest req=mock(HttpServletRequest.class);
        HttpServletResponse resp=mock(HttpServletResponse.class);
        when(req.getRequestURI()).thenReturn("/api/usuarios");
        doThrow(new IllegalStateException("Banco indisponível")).when(auditoria)
                .registrarAcessoNegado("/api/usuarios");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "atendente", "N/A", List.of(new SimpleGrantedAuthority("ROLE_ATENDENTE"))));
        new AcessoNegadoHandler(auditoria).handle(req,resp,new AccessDeniedException("Proibido"));
        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
