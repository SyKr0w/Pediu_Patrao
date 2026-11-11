/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umc.pediupatrao.controller;

/**
 *
 * @author nocta
 */
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PainelWebController {

    @GetMapping("/painel")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public String pagina(Model model) {
        model.addAttribute("content", "painel :: content");
        return "layout";
    }
}
