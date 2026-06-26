package cl.inostratech.tienda.repository;

import cl.inostratech.tienda.model.EstadoPedido;
import cl.inostratech.tienda.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByFechaCreacionDesc();

    List<Pedido> findByEstado(EstadoPedido estado);

    long countByEstado(EstadoPedido estado);
}