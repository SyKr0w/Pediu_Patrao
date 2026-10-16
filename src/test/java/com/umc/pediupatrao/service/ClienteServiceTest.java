package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.AlteracaoCampo;
import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock ClienteRepository clientes;
    @Mock AuditoriaService auditoria;
    @Test void consultaDeNomesNaoRetornaOsOutrosDadosCadastrais() {
        Cliente pessoa=new Cliente();pessoa.setId("c1");pessoa.setNome("Maria");pessoa.setTelefone("11999999999");
        when(clientes.findAll()).thenReturn(List.of(pessoa));
        ClienteService service=new ClienteService(clientes,auditoria);
        assertEquals(Map.of("c1","Maria"),service.nomesParaConsultaPedidos());
    }

    @Test void alterarTelefoneRegistraValorAnteriorENovo() {
        Cliente antigo=new Cliente();antigo.setId("cli1");antigo.setNome("Maria");antigo.setTelefone("11111111");
        Cliente recebido=new Cliente();recebido.setId("cli1");recebido.setNome("Maria");recebido.setTelefone("22222222");
        when(clientes.findById("cli1")).thenReturn(Optional.of(antigo));
        when(clientes.save(any(Cliente.class))).thenAnswer(invocation->invocation.getArgument(0));
        ClienteService service=new ClienteService(clientes,auditoria);
        Cliente atualizado=service.salvar(recebido);
        assertEquals("22222222",atualizado.getTelefone());
        @SuppressWarnings("unchecked") ArgumentCaptor<Map<String,AlteracaoCampo>> captor=ArgumentCaptor.forClass(Map.class);
        verify(auditoria).registrar(eq("ALTERACAO_CLIENTE"),eq("CLIENTE"),eq("cli1"),captor.capture(),isNull());
        AlteracaoCampo alteracao=captor.getValue().get("telefone");
        assertNotNull(alteracao);
        assertEquals("11111111",alteracao.getAnterior());
        assertEquals("22222222",alteracao.getNovo());
    }
}
