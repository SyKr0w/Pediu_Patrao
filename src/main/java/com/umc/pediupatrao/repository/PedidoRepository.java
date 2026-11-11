package com.umc.pediupatrao.repository;
import org.springframework.data.mongodb.repository.Query;

import com.umc.pediupatrao.entity.Pedido;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.time.LocalDateTime;

public interface PedidoRepository extends MongoRepository<Pedido, String> {
    List<Pedido> findByClienteId(String clienteId);

    @Query("{ 'entradaEm': { '$gte': ?0, '$lt': ?1 } }")
    List<Pedido> findByEntradaEmGreaterThanEqualAndEntradaEmLessThan(LocalDateTime inicio, LocalDateTime fim);
    @Query("{ 'finalizadoEm': { '$gte': ?0, '$lt': ?1 } }")
    List<Pedido> findByFinalizadoEmGreaterThanEqualAndFinalizadoEmLessThan(LocalDateTime inicio, LocalDateTime fim);

}
