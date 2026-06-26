package cl.inostratech.tienda.pedido.dto;

import cl.inostratech.tienda.model.ItemPedido;
import cl.inostratech.tienda.model.MetodoPago;
import cl.inostratech.tienda.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representacion de un pedido devuelta al cliente/admin.
 */
public record PedidoResponse(
        Long id,
        /** Numero de pedido legible, ej: "#00123". */
        String numeroPedido,
        String nombreCliente,
        String telefonoCliente,
        String emailCliente,
        String comentario,
        MetodoPago metodoPago,
        String estado,
        BigDecimal total,
        List<ItemResponse> items,
        LocalDateTime fechaCreacion
) {

    public record ItemResponse(
            Long id,
            Long productoId,
            String nombreProducto,
            BigDecimal precioUnitario,
            Integer cantidad,
            BigDecimal subtotal
    ) {
        static ItemResponse desde(ItemPedido i) {
            return new ItemResponse(
                    i.getId(),
                    i.getProducto() != null ? i.getProducto().getId() : null,
                    i.getNombreProducto(),
                    i.getPrecioUnitario(),
                    i.getCantidad(),
                    i.getSubtotal());
        }
    }

    public static PedidoResponse desde(Pedido p) {
        List<ItemResponse> items = p.getItems().stream().map(ItemResponse::desde).toList();
        return new PedidoResponse(
                p.getId(),
                formatearNumero(p.getId()),
                p.getNombreCliente(),
                p.getTelefonoCliente(),
                p.getEmailCliente(),
                p.getComentario(),
                p.getMetodoPago(),
                p.getEstado().name(),
                p.getTotal(),
                items,
                p.getFechaCreacion());
    }

    /** Formatea el id como numero de pedido con ceros a la izquierda: 123 -> "#00123". */
    public static String formatearNumero(Long id) {
        return id == null ? "#-----" : String.format("#%05d", id);
    }
}