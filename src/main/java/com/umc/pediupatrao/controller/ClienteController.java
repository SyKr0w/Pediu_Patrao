package com.umc.pediupatrao.controller;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.service.ClienteService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;
    public ClienteController(ClienteService service){this.service=service;}
    @GetMapping public List<Cliente> listar(){return service.listarClientes();}
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Cliente criar(@RequestBody Cliente c){return service.novoCliente(c);}
    @PutMapping("/{id}") public Cliente atualizar(@PathVariable String id,@RequestBody Cliente c){c.setId(id);return service.salvar(c);}
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void excluir(@PathVariable String id){service.excluir(id);}
}
