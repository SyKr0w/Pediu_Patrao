package com.umc.pediupatrao.controller;
import com.umc.pediupatrao.entity.Auditoria;
import com.umc.pediupatrao.service.AuditoriaService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {
    private final AuditoriaService service;
    public AuditoriaController(AuditoriaService service){this.service=service;}
    @GetMapping public List<Auditoria> listar(){return service.listar();}
}
