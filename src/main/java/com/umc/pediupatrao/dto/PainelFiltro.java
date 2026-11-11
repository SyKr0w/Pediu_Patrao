/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.umc.pediupatrao.dto;

/**
 *
 * @author nocta
 */
import java.time.LocalDate;

public record PainelFiltro(LocalDate inicio, LocalDate fim, String status, String produtoId) {
}
