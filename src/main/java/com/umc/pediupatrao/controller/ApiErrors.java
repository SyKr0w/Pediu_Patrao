package com.umc.pediupatrao.controller;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice(basePackages="com.umc.pediupatrao.controller")
public class ApiErrors {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e){return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro",e.getMessage()));}
}
