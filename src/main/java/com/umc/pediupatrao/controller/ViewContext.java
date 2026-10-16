package com.umc.pediupatrao.controller;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
@ControllerAdvice
public class ViewContext {
    @ModelAttribute("perfilAtual") public String roleAtual(){
        Authentication a=SecurityContextHolder.getContext().getAuthentication();
        if(a==null)return "";
        return a.getAuthorities().stream().map(x->x.getAuthority()).filter(x->x.startsWith("ROLE_")).map(x->x.substring(5)).findFirst().orElse("");
    }
    @ModelAttribute("nomeAtual") public String usuarioAtual(){
        Authentication a=SecurityContextHolder.getContext().getAuthentication();return a==null?"":a.getName();
    }
}
