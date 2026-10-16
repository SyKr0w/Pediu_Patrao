package com.umc.pediupatrao.config;

import com.umc.pediupatrao.service.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class AcessoNegadoHandler implements AccessDeniedHandler {
    private static final Logger log=LoggerFactory.getLogger(AcessoNegadoHandler.class);
    private final AuditoriaService auditoria;
    public AcessoNegadoHandler(AuditoriaService auditoria){this.auditoria=auditoria;}

    @Override
    public void handle(HttpServletRequest req,HttpServletResponse resp,AccessDeniedException ex)
            throws IOException, ServletException {
        Authentication usuario=SecurityContextHolder.getContext().getAuthentication();
        if(usuario!=null && usuario.isAuthenticated() && !(usuario instanceof AnonymousAuthenticationToken)){
            try{
                // Sem query string, senha, token, headers ou conteúdo da requisição.
                auditoria.registrarAcessoNegado(req.getRequestURI());
            }catch(RuntimeException e){
                // Falha no registro não pode transformar o bloqueio de segurança em erro 500.
                log.warn("Não foi possível persistir o evento de acesso negado.");
            }
        }
        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
