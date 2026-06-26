package cl.inostratech.tienda.repository;

import cl.inostratech.tienda.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}