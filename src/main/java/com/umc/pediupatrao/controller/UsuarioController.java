package com.umc.pediupatrao.controller;
import com.umc.pediupatrao.entity.Usuario;
import com.umc.pediupatrao.service.UsuarioService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service){this.service=service;}
    @GetMapping public List<Usuario> listar(){return service.listarTodos();}
    @GetMapping("/{id}") public Usuario buscar(@PathVariable String id){return service.buscarPorId(id).orElseThrow(()->new IllegalArgumentException("Usuário não encontrado."));}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Usuario criar(@RequestBody Usuario usuario){return service.salvarUsuario(usuario);}
    @PutMapping("/{id}") public Usuario atualizar(@PathVariable String id,@RequestBody Usuario usuario){return service.atualizarUsuario(id,usuario);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable String id){service.deletarUsuario(id);}
}
