package com.umc.pediupatrao.controller;
import com.umc.pediupatrao.entity.*;
import com.umc.pediupatrao.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Controller
public class HomeController {
    private final UsuarioService usuarios;
    private final ClienteService clientes;
    private final ProdutoService produtos;
    public HomeController(UsuarioService u,ClienteService c,ProdutoService p){usuarios=u;clientes=c;produtos=p;}
    @GetMapping("/login") public String login(){return "login";}
    @GetMapping("/configuracoes") public String configuracoes(Model m){return pagina(m,"configuracoes :: content");}
    @GetMapping("/") public String home(Model m){m.addAttribute("content","home :: content");return "layout";}
    @GetMapping("/usuarios") public String usuarios(Model m){m.addAttribute("usuarios",usuarios.listarTodos());return pagina(m,"usuarios/lista :: content");}
    @GetMapping("/usuarios/novo") public String usuarioNovo(Model m){m.addAttribute("usuario",new Usuario());return pagina(m,"usuarios/form :: content");}
    @GetMapping("/usuarios/editar/{id}") public String usuarioEditar(@PathVariable String id,Model m){
        Usuario origem=usuarios.buscarPorId(id).orElseThrow(()->new IllegalArgumentException("Usuário não encontrado."));
        Usuario u=new Usuario();u.setId(origem.getId());u.setUsername(origem.getUsername());u.setRole(origem.getRole());
        m.addAttribute("usuario",u);return pagina(m,"usuarios/form :: content");
    }
    @PostMapping("/usuarios/salvar") public String usuarioSalvar(@ModelAttribute Usuario u,RedirectAttributes r){
        try{usuarios.salvarUsuario(u);r.addFlashAttribute("sucesso","Usuário criado.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/usuarios";
    }
    @PostMapping("/usuarios/editar/{id}") public String usuarioAtualizar(@PathVariable String id,@ModelAttribute Usuario u,RedirectAttributes r){
        try{usuarios.atualizarUsuario(id,u);r.addFlashAttribute("sucesso","Usuário atualizado.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/usuarios";
    }
    @PostMapping("/usuarios/deletar/{id}") public String usuarioApagar(@PathVariable String id,RedirectAttributes r){
        try{usuarios.deletarUsuario(id);r.addFlashAttribute("sucesso","Usuário removido.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/usuarios";
    }
    @GetMapping("/clientes") public String clientes(Model m){m.addAttribute("clientes",clientes.listarClientes());return pagina(m,"clientes/lista :: content");}
    @GetMapping("/clientes/novo") public String clienteNovo(Model m){m.addAttribute("cliente",new Cliente());return pagina(m,"clientes/form :: content");}
    @GetMapping("/clientes/editar/{id}") public String clienteEditar(@PathVariable String id,Model m){
        m.addAttribute("cliente",clientes.buscarPorId(id).orElseThrow(()->new IllegalArgumentException("Cliente não encontrado.")));
        return pagina(m,"clientes/form :: content");
    }
    @PostMapping("/clientes/salvar") public String clienteSalvar(@ModelAttribute Cliente c,RedirectAttributes r){
        try{clientes.novoCliente(c);r.addFlashAttribute("sucesso","Cliente criado.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/clientes";
    }
    @PostMapping("/clientes/editar/{id}") public String clienteAtualizar(@PathVariable String id,@ModelAttribute Cliente c,RedirectAttributes r){
        try{c.setId(id);clientes.salvar(c);r.addFlashAttribute("sucesso","Cliente atualizado.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/clientes";
    }
    @PostMapping("/clientes/excluir/{id}") public String clienteExcluir(@PathVariable String id,RedirectAttributes r){
        try{clientes.excluir(id);r.addFlashAttribute("sucesso","Cliente excluído.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/clientes";
    }
    @GetMapping("/produtos") public String produtos(Model m){m.addAttribute("produtos",produtos.listarProdutos());return pagina(m,"produtos/lista :: content");}
    @GetMapping("/produtos/novo") public String produtoNovo(Model m){m.addAttribute("produto",new Produto());return pagina(m,"produtos/produto-form :: content");}
    @GetMapping("/produtos/editar/{id}") public String produtoEditar(@PathVariable String id,Model m){
        m.addAttribute("produto",produtos.buscarPorId(id).orElseThrow(()->new IllegalArgumentException("Produto não encontrado.")));
        return pagina(m,"produtos/produto-form :: content");
    }
    @PostMapping("/produtos/salvar") public String produtoSalvar(@ModelAttribute Produto p,RedirectAttributes r){
        try{produtos.salvar(p);r.addFlashAttribute("sucesso","Produto salvo.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/produtos";
    }
    @PostMapping("/produtos/excluir/{id}") public String produtoExcluir(@PathVariable String id,RedirectAttributes r){
        try{produtos.excluir(id);r.addFlashAttribute("sucesso","Produto excluído.");}catch(IllegalArgumentException e){r.addFlashAttribute("erro",e.getMessage());}
        return "redirect:/produtos";
    }
    private String pagina(Model m,String frag){m.addAttribute("content",frag);return "layout";}
}
