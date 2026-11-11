/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umc.pediupatrao.dto;

/**
 *
 * @author nocta
 */
import java.math.BigDecimal;
import java.time.LocalDate;

public class PainelResposta {

    public record Resumo(BigDecimal faturamento, long pedidosFinalizados,
                         BigDecimal ticketMedio, long cancelamentos, long pedidosRegistrados) {
    }

    public record VendaDia(LocalDate data, BigDecimal total, long pedidos) {
    }

    public record Situacao(String status, String descricao, long quantidade) {
    }

    public record Popular(String produtoId, String nome, long quantidade) {
    }

    public record ProdutoOpcao(String id, String nome) {
    }
}
